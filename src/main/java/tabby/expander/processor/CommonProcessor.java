package tabby.expander.processor;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import tabby.calculator.Calculator;
import tabby.data.State;
import tabby.util.JsonHelper;
import tabby.util.Types;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
public class CommonProcessor extends BaseProcessor{

    private boolean isAbstract = false;
    private boolean isFromAbstractClass = false;

    @Override
    public void init(Node node, State preState, Relationship lastRelationship, Calculator calculator) {
        super.init(node, preState, lastRelationship, calculator);

//        Map<String, Object> properties = node.getProperties("IS_ABSTRACT", "IS_FROM_ABSTRACT_CLASS");
//        this.isAbstract = (boolean) properties.getOrDefault("IS_ABSTRACT", false);
//        this.isFromAbstractClass = (boolean) properties.getOrDefault("IS_FROM_ABSTRACT_CLASS", false);
    }

    @Override
    public Relationship process(Relationship next) {
        Relationship ret = null;
        String nextId = next.getId() + "";
        if(Types.isAlias(next)){
            nextState.put(nextId, polluted.stream().mapToInt(Integer::intValue).toArray());
            nextState.addAliasEdge(next.getId());
            ret = next;
        }else{
            String pollutedStr = (String) next.getProperty("POLLUTED_POSITION");
            if(pollutedStr == null) return ret;
            int[][] callSite = JsonHelper.parse(pollutedStr);

//            // 为了处理当前设计的代码属性图的缺点，但测试后发现丢失的情况很多，暂不处理
//            if((!isAbstract || !isFromAbstractClass) && !PositionHelper.isCallerPolluted(callSite, polluted)){ // 如果当前调用边的调用者不可控，则下一次不进行alias操作
//                nextState.getNextAlias().add(next.getId());
//            }

            int[] nextPos = calculator.calculate(callSite, polluted);
            if(nextPos != null && nextPos.length > 0){
                nextState.put(nextId, nextPos);
                ret = next;
            }
        }
        return ret;
    }

    @Override
    public boolean isNeedProcess() {
        return true;
    }

    @Override
    public State getNextState() {
        return nextState;
    }
}
