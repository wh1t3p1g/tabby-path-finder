package tabby.expander.processor;

import org.neo4j.graphdb.Relationship;
import tabby.util.JsonHelper;
import tabby.util.PositionHelper;
import tabby.data.State;
import tabby.util.Types;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
public class CommonProcessor extends BaseProcessor{

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
            if(!PositionHelper.isCallerPolluted(callSite, polluted)){ // 如果当前调用边的调用者不可控，则下一次不进行alias操作
                nextState.getNextAlias().add(next.getId());
            }
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
