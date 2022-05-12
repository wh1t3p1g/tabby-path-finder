package tabby.expander.processor;

import org.neo4j.graphdb.Relationship;
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
            if(polluted.contains(PositionHelper.THIS)){ // 当前调用者是可控的
                nextState.put(nextId, polluted.stream().mapToInt(Integer::intValue).toArray());
                nextState.addAliasEdge(next.getId());
                ret = next;
            }
        }else{
            String pollutedStr = (String) next.getProperty("POLLUTED_POSITION");
            if(pollutedStr == null) return ret;

            int[] nextPos = calculator.calculate(pollutedStr, polluted);
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
