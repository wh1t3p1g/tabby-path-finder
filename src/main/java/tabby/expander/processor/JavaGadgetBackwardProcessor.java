package tabby.expander.processor;

import org.neo4j.graphdb.Relationship;
import tabby.data.EdgeCache;
import tabby.util.PositionHelper;
import tabby.util.Transformer;
import tabby.util.Types;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
public class JavaGadgetBackwardProcessor extends JavaGadgetProcessor{

    @Override
    public Relationship process(Relationship next){
        Relationship ret = null;
        long nextEdgeId = next.getId();
        String key = nextEdgeId + "";

        if(Types.isAlias(next)){
            nextState.put(key, Transformer.setToIntArray(polluted));
            nextState.addAliasEdge(nextEdgeId);
            ret = next;
        }else{
            int[][] callSite = EdgeCache.rel.get(next);

            if(isLastRelationshipTypeAlias && callSite.length > 0
                    && PositionHelper.isNotPollutedPosition(callSite[0])){
                return null;
            }

            int[] nextPos = calculator.calculate(callSite, polluted);

            if(nextPos != null && nextPos.length > 0){
                nextState.put(key, nextPos);
                ret = next;
            }
        }
        return ret;
    }

    @Override
    public boolean isNeedProcess() {
        return isFirstNode || isSerializable() || isStatic() ||
                isAbstract() || isFromAbstractClass() ||
                "java.lang.Object".equals(getClassname());
    }

    @Override
    public Processor copy() {
        return new JavaGadgetBackwardProcessor();
    }
}
