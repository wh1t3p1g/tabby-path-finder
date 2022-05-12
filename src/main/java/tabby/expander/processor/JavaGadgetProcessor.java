package tabby.expander.processor;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import tabby.calculator.Calculator;
import tabby.util.JsonHelper;
import tabby.util.PositionHelper;
import tabby.data.State;
import tabby.util.Types;

import java.util.Map;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
public class JavaGadgetProcessor extends BaseProcessor{

    private boolean isSerializable = false;
    private boolean isAbstract = false;
    private boolean isStatic = false;
    private boolean isFromAbstractClass = false;
    private String classname = null;


    @Override
    public void init(Node node, State preState, Relationship lastRelationship, Calculator calculator){
        super.init(node, preState, lastRelationship, calculator);

        Map<String, Object> properties = node.getProperties("IS_SERIALIZABLE", "IS_ABSTRACT", "IS_STATIC", "IS_FROM_ABSTRACT_CLASS","CLASSNAME");
        this.isSerializable = (boolean) properties.getOrDefault("IS_SERIALIZABLE", false);
        this.isAbstract = (boolean) properties.getOrDefault("IS_ABSTRACT", false);
        this.isStatic = (boolean) properties.getOrDefault("IS_STATIC", false);
        this.isFromAbstractClass = (boolean) properties.getOrDefault("IS_FROM_ABSTRACT_CLASS", false);
        this.classname = (String) properties.getOrDefault("CLASSNAME", "java.lang.Object");
    }

    @Override
    public Relationship process(Relationship next){
        Relationship ret = null;
        String nextId = next.getId() + "";
        if(Types.isAlias(next)){
            nextState.put(nextId, polluted.stream().mapToInt(Integer::intValue).toArray());
            nextState.addAliasEdge(next.getId());
            if(lastRelationship != null && preState.isStaticCall(lastRelationship.getId())){
                nextState.addStaticCallEdge(next.getId());
            }
            ret = next;
        }else{
            String pollutedStr = (String) next.getProperty("POLLUTED_POSITION");
            if(pollutedStr == null) return ret;
            int[][] callSite = JsonHelper.parse(pollutedStr);

            if(!PositionHelper.isCallerPolluted(callSite, polluted)){ // 如果当前调用边的调用者不可控，则下一次不进行alias操作
                nextState.getNextAlias().add(next.getId());
            }

            int[] nextPos = calculator.calculate(callSite, polluted);
            if(nextPos != null && nextPos.length > 0){
                nextState.put(nextId, nextPos);
                if(isStatic){
                    nextState.addStaticCallEdge(next.getId());
                }
                ret = next;
            }
        }
        return ret;
    }

    @Override
    public boolean isNeedProcess() {
        if(isSerializable || isStatic || isAbstract || isFromAbstractClass || "java.lang.Object".equals(classname)) return true;

        return lastRelationship != null && preState.isStaticCall(lastRelationship.getId()); // 如果上层是静态调用，则默认允许下一层可以不符合上面的条件
    }

    @Override
    public State getNextState() {
        return nextState;
    }
}
