package tabby.expander;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import tabby.util.State;
import tabby.util.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wh1t3p1g
 * @since 2022/4/26
 */
public class MonoDirectionalPathExpander implements PathExpander<State> {

    private final Direction direction;
    private final RelationshipType[] relationshipTypes;

    public MonoDirectionalPathExpander() {
        String[] types = new String[]{"<CALL", "<ALIAS"};
        direction = Types.directionFor(types[0]);
        relationshipTypes
                = new RelationshipType[]{
                Types.relationshipTypeFor(types[0]),
                Types.relationshipTypeFor(types[1])
        };
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<State> state) {
        final Node node = path.endNode();
        final Relationship lastRelationship = path.lastRelationship();
        State preState = state.getState();

        int[] current = null;
        if(lastRelationship == null){
            current = preState.getPositions("initial");
        }else{
            String id = (String) lastRelationship.getProperty("ID");
            current = preState.getPositions(id);
        }

        State nextState = State.newInstance();
        Iterable<Relationship> relationships = node.getRelationships(direction, relationshipTypes);
        List<Relationship> nextRelationships = new ArrayList<>();
        for(Relationship next:relationships){
            String nextId = (String) next.getProperty("ID");
            if(Types.isAlias(next.getType())){
                nextState.put(nextId, current);
                nextRelationships.add(next);
            }else{
                String polluted = (String) next.getProperty("POLLUTED_POSITION", "[]");
                int[] nextPos = preState.test(current, polluted);
                if(nextPos != null){
                    nextState.put(nextId, nextPos);
                    nextRelationships.add(next);
                }
            }
        }
        state.setState(nextState);
        return nextRelationships;
    }

    @Override
    public PathExpander<State> reverse() {
        return null;
    }
}
