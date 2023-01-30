package tabby.expander.processor;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import tabby.calculator.BackwardCalculator;
import tabby.data.EdgeCache;
import tabby.data.State;
import tabby.util.PositionHelper;
import tabby.util.Types;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
public class CommonProcessor extends BaseProcessor{

    @Override
    public void init(Node node, State preState, Relationship lastRelationship) {
        super.init(node, preState, lastRelationship);
    }

    @Override
    public Relationship process(Relationship next) {
        Relationship ret = null;
        String nextId = next.getId() + "";
        if(Types.isAlias(next)){
            if(polluted.contains(PositionHelper.THIS)){
                nextState.put(nextId, polluted.stream().mapToInt(Integer::intValue).toArray());
                nextState.addAliasEdge(next.getId());
                ret = next;
            }
        }else{
            int[][] callSite = EdgeCache.rel.get(next);

            if(isNecessaryProcess(callSite)){
                int[] nextPos = calculator.calculate(callSite, polluted);

                if(nextPos != null && nextPos.length > 0){
                    nextState.put(nextId, nextPos);
                    ret = next;
                }
            }
        }
        return ret;
    }

    public boolean isNecessaryProcess(int[][] callSite){
        if(calculator instanceof BackwardCalculator){
            // isLastRelationshipTypeAlias 用于排除
            if(isLastRelationshipTypeAlias && callSite.length > 0
                    && PositionHelper.isNotPollutedPosition(callSite[0])){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isNeedProcess() {
        return true;
    }
}
