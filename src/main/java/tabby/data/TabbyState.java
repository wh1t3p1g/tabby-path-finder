package tabby.data;

import lombok.Getter;
import lombok.Setter;
import org.neo4j.graphdb.Node;

import java.util.*;

/**
 * 继承 上节点对应的状态 和 类型
 * Table<NodeId, Position, List<polluted, type, pre_polluted>>
 * @author wh1t3p1g
 * @since 2023/8/22
 */
@Getter
@Setter
public class TabbyState {

    private Map<String, Pollution> state = new HashMap<>();

    public void put(String id, Pollution pollution){
        state.put(id, pollution);
    }

    public Pollution get(String id){
        return state.get(id);
    }

    public static TabbyState of(){
        return new TabbyState();
    }

    public static TabbyState initialState(Node node, String polluteJson){
        try{
            TabbyState tabbyState = new TabbyState();
            Pollution pollution = new Pollution();

            if(polluteJson != null){
                pollution.setPollutedFromJson(polluteJson);
            }else{
                String json = (String) node.getProperty("POLLUTED_POSITION", "[]");
                pollution.setPollutedFromJson(json);
            }
            String id = "node_" + node.getId();
            tabbyState.put(id, pollution);
            return tabbyState;
        }catch (Exception ig){}
        return null;
    }

    public static TabbyState initialState(Node node){
        try{
            TabbyState tabbyState = new TabbyState();
            String subSignature = (String) node.getProperty("SUB_SIGNATURE", "");
            String classname = (String) node.getProperty("CLASSNAME", "");
            String id = "node_"+node.getId();

            List<Set<Integer>> polluted = new LinkedList<>();
            List<Set<String>> types = new LinkedList<>();

            polluted.add(Set.of(-1));
            types.add(Set.of(classname));

            String[] parameters = getParameterTypes(subSignature);
            int index = 0;
            for(String type:parameters){
                polluted.add(Set.of(index++));
                types.add(Set.of(type));
            }

            tabbyState.put(id, Pollution.of(polluted, types, null));
            return tabbyState;
        }catch (Exception ig){}

        return null;
    }

    public static String[] getParameterTypes(String subSignature){
        int index = subSignature.indexOf("(");
        String sub = subSignature.substring(index+1);
        sub = sub.substring(0, sub.length()-1);
        if(sub.contains(",")){
            return sub.split(",");
        }else if(!sub.isEmpty()){
            return new String[]{sub};
        }else{
            return new String[0];
        }
    }


}
