package tabby.data;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import tabby.util.JsonHelper;
import tabby.util.PositionHelper;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author wh1t3p1g
 * @since 2023/8/23
 */
@Data
public class Pollution {

    private List<Set<Integer>> polluted = new LinkedList<>();

    private List<Set<String>> types = new LinkedList<>();

//    private Map<Integer, Set<Integer>> specialPolluted = new HashMap<>();
    private SetMultimap<Integer, Integer> specialPolluted = MultimapBuilder.hashKeys().hashSetValues().build();
    private boolean isCallerInstanceObj = false;

    public void setPollutedFromJson(String json){
        Type objectType = new TypeToken<List<Set<Integer>>>(){}.getType();
        List<Set<Integer>> p = JsonHelper.parseObject(json, objectType);
        if(p != null){
            setPolluted(p);
        }
    }

    public void setTypesFromJson(String json){
        Type objectType = new TypeToken<List<Set<String>>>(){}.getType();
        List<Set<String>> p = JsonHelper.parseObject(json, objectType);
        if(p != null){
            setTypes(p);
        }
    }

    public Set<Integer> getFlatPolluted(){
        Set<Integer> ret = new HashSet<>();
        for(Set<Integer> p:polluted){
            ret.addAll(p);
        }
        return ret;
    }

    public Set<String> getTypes(int index){
        if(types.size() < index) return new HashSet<>();
        return types.get(index);
    }

    public boolean isContainsAuth(){
        Set<Integer> flatSet = getFlatPolluted();

        if(flatSet.contains(PositionHelper.AUTH)){
            return true;
        }else{
            for(int index:flatSet){
                if(specialPolluted.containsKey(index)){
                    return true;
                }
            }
        }
        return false;
    }

    // 正向算法 forward，生成下一个 pollution
    public static Pollution getNextPollution(Pollution pre, Pollution cur){
        List<Set<Integer>> next = new LinkedList<>();
        List<Set<String>> nextTypes = new LinkedList<>();
        SetMultimap<Integer, Integer> specialPolluted = MultimapBuilder.hashKeys().hashSetValues().build();
        SetMultimap<Integer, Integer> preSpecialPolluted = pre.getSpecialPolluted();

        Set<Integer> pol = pre.getFlatPolluted();
        pol.add(PositionHelper.SOURCE);
        pol.add(PositionHelper.AUTH);
        pol.remove(PositionHelper.NOT_POLLUTED_POSITION);

        List<Set<Integer>> callSite = cur.getPolluted();
        int length = callSite.size();
        for(int index = 0; index < length; index++){
            Set<Integer> sub = callSite.get(index);
            if(sub.contains(PositionHelper.AUTH)){ // 创建新的指向
                specialPolluted.put(index-1, PositionHelper.AUTH);
            }
            boolean flag = true;
            Set<String> newTypes = new HashSet<>();
            newTypes.addAll(cur.getTypes(index));
            for(int p:sub){
                if(pol.contains(p)){
                    if(flag){
                        next.add(Set.of(index-1));
                        flag = false;
                    }
                    int pos = p+1;
                    if(pos < pre.getTypes().size() && pos >= 0 && !cur.isCallerInstanceObj()){
                        newTypes.addAll(pre.getTypes(pos));
                    }
                }

                if(preSpecialPolluted.containsKey(p)){
                    specialPolluted.putAll(index-1, preSpecialPolluted.get(p));
                }
            }
            if(newTypes.size() > 0){
                nextTypes.add(newTypes);
            }
        }

        if(next.isEmpty()){
            return null;
        }

        return Pollution.of(next, nextTypes, specialPolluted);
    }

    // backward
    public static Pollution getNextPollutionReverse(Pollution pre, Pollution cur){
        if(pre.isEmpty()) return null;
        List<Set<Integer>> nextPolluted = new LinkedList<>();
        List<Set<Integer>> callSite = cur.getPolluted();
        List<Set<Integer>> prePolluted = pre.getPolluted();
        int length = callSite.size();
        boolean flag = true;
        for(Set<Integer> prePol: prePolluted){
            Set<Integer> temp = new HashSet<>();
            for(int p : prePol){
                int pos = p + 1;
                if(length > pos && pos >= 0){
                    Set<Integer> curSite = callSite.get(pos);
                    if(curSite != null){
                        temp.addAll(curSite);
                    }
                }else if(p == PositionHelper.AUTH || p == PositionHelper.SOURCE){
                    temp.add(p);
                }
            }
            if(temp.isEmpty() || (temp.size() == 1 && temp.contains(PositionHelper.NOT_POLLUTED_POSITION))){
                flag = false;
                break;
            }else{
                temp.remove(PositionHelper.NOT_POLLUTED_POSITION);
                nextPolluted.add(temp);
            }
        }
        if(flag){
            return Pollution.of(nextPolluted, null, null);
        }

        return null;
    }

    public boolean isEmpty(){
        return polluted.isEmpty();
    }

    public static Pollution of(Relationship edge){
        try{
            Pollution pollution = new Pollution();
            pollution.setPollutedFromJson((String) edge.getProperty("POLLUTED_POSITION", "[]"));
            pollution.setTypesFromJson((String) edge.getProperty("TYPES", "[]"));
            pollution.setCallerInstanceObj((boolean) edge.getProperty("IS_CALLER_THIS_FIELD_OBJ", false));
            return pollution;
        }catch (Exception ig){}

        return null;
    }

    public static Pollution of(List<Set<Integer>> polluted, List<Set<String>> types, SetMultimap<Integer, Integer> specialPolluted){
        Pollution pollution = new Pollution();
        pollution.setPolluted(polluted);
        pollution.setTypes(types);
        if(specialPolluted != null){
            pollution.setSpecialPolluted(specialPolluted);
        }
        return pollution.copy();
    }

    public Pollution copy(){
        Pollution copied = new Pollution();
        Type objectType = new TypeToken<List<Set<Integer>>>(){}.getType();
        copied.setPolluted(JsonHelper.deepCopy(polluted, objectType));
        objectType = new TypeToken<List<Set<String>>>(){}.getType();
        copied.setTypes(JsonHelper.deepCopy(types, objectType));
        Map<Integer, Collection<Integer>> map = specialPolluted.asMap();

        for(Map.Entry<Integer, Collection<Integer>> entry: map.entrySet()){
            copied.specialPolluted.putAll(entry.getKey(), entry.getValue());
        }
        return copied;
    }

    public static boolean compare(Pollution pre, Pollution cur){
        List<Set<Integer>> sinkPol = cur.getPolluted();
        Set<Integer> callPol = pre.getFlatPolluted();

        for(Set<Integer> sink:sinkPol){
            boolean flag = true;
            for(int p:sink){
                if(callPol.contains(p)){
                    flag = false;
                    break;
                }
            }
            if(flag){
                return false;
            }
        }
        return true;
    }

    public static Pollution pure(Node start, Node end, Pollution cur){
        Pollution nextPollution = cur.copy();

        // pure types
        List<Set<String>> types = nextPollution.getTypes();

        if(!types.isEmpty()){
            Set<String> baseObjTypes = types.get(0);
            if(baseObjTypes.size() > 0){
                try{
                    String startNodeClazz = (String) start.getProperty("CLASSNAME");
                    if(startNodeClazz != null && !startNodeClazz.isBlank()){
                        baseObjTypes.remove(startNodeClazz);
                    }

                    if(baseObjTypes.size() > 0){
                        boolean isAbstract = (boolean) end.getProperty("IS_ABSTRACT", false);
                        String endNodeClazz = (String) end.getProperty("CLASSNAME");

                        if(!isAbstract && endNodeClazz != null && !endNodeClazz.isBlank() && !baseObjTypes.contains(endNodeClazz)){
                            // 如果下一个节点还是abstract，则可能下一条边还是alias，继续往下传
                            // 如果下一个节点不是abstract，并且当前types里面没有当前的endNode的类型，则不继续往下传
                            return null;
                        }
                    }
                }catch (Exception ig){}
            }
            nextPollution.setTypes(types);
        }

        return nextPollution;
    }

}
