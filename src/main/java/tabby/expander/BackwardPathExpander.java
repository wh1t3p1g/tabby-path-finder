package tabby.expander;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import tabby.util.JsonHelper;

import java.util.Map;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class BackwardPathExpander extends BasePathExpander.RegularExpander{

    private int[] polluted = null;

    BackwardPathExpander(Map<Direction, RelationshipType[]> types) {
        super(types);
    }

    @Override
    ResourceIterator<Relationship> doExpand(Path path, BranchState state) {
        final Node node = path.endNode();

        boolean propagated = true;
        initPollutedPositions(node, state.getState());

        if(polluted != null && polluted.length > 0){
            // check
            // get all call edges
            DirectionAndTypes call = getDirectionAndTypes("CALL");
            Iterable<Relationship> callEdges = node.getRelationships( call.direction, call.types );

            // match state and calculate next state
            // TODO 这部分可以用多线程来处理
        }

        return super.doExpand(path, state);
    }

    public void initPollutedPositions(Node node, Object state){
        if(state == null && polluted == null){ // 起始节点
            boolean isSink = (boolean) node.getProperty("IS_SINK", false);
            if(isSink){
                String positionStr = (String) node.getProperty("POLLUTED_POSITION", "[]");
                polluted = JsonHelper.parsePollutedPosition(positionStr);
            }
        }else if(state != null){
            polluted = (int[]) state;
        }
    }
}
