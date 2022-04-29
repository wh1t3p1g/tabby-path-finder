package tabby.expander;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import tabby.util.State;
import tabby.util.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author wh1t3p1g
 * @since 2022/4/26
 */
public class MonoDirectionalPathExpander implements PathExpander<State> {

    private final Direction direction;
    private final RelationshipType[] relationshipTypes;
    private boolean parallel = false;

    public MonoDirectionalPathExpander(boolean parallel) {
        String[] types = new String[]{"<CALL", "<ALIAS"};
        direction = Types.directionFor(types[0]);
        relationshipTypes
                = new RelationshipType[]{
                    Types.relationshipTypeFor(types[0]),
                    Types.relationshipTypeFor(types[1])
                };
        this.parallel = parallel;
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<State> state) {
        final Node node = path.endNode();
        final Relationship lastRelationship = path.lastRelationship();
        State preState = state.getState();

        int[] current = null;
        boolean isLastRelationshipTypeAlias = false;
        if(lastRelationship == null){
            current = preState.getPositions("initial");
        }else{
            String id = lastRelationship.getId() + "";
            current = preState.getPositions(id);
            isLastRelationshipTypeAlias = Types.isAlias(lastRelationship);
        }

        State nextState = State.newInstance();
        Iterable<Relationship> relationships = node.getRelationships(direction, relationshipTypes);
        List<Relationship> nextRelationships = new ArrayList<>();

        int[] finalCurrent = current;
        boolean finalIsLastRelationshipTypeAlias = isLastRelationshipTypeAlias;
        nextRelationships = StreamSupport.stream(relationships.spliterator(), parallel)
                .map((next) ->
                        process(next, finalIsLastRelationshipTypeAlias,
                                finalCurrent, nextState))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        state.setState(nextState);
        return nextRelationships;
    }

    public Relationship process(Relationship currentRelationship,
                                boolean isLastRelationshipTypeAlias,
                                int[] polluted, State state){
        Relationship ret = null;
        String nextId = currentRelationship.getId() + "";
        if(Types.isAlias(currentRelationship)){
            state.put(nextId, polluted);
            state.addAliasEdge(currentRelationship.getId());
            ret = currentRelationship;
        }else{
            String pollutedStr = getData(currentRelationship,"POLLUTED_POSITION");
            if(pollutedStr == null) return ret;
            int[] nextPos = state.test(polluted, pollutedStr, isLastRelationshipTypeAlias);
            if(nextPos != null && nextPos.length > 0){
                state.put(nextId, nextPos);
                ret = currentRelationship;
            }
        }
        return ret;
    }

    // 多线程的情况下，可能会产生获取不到内容的情况，重复取3次
    public String getData(Relationship relationship, String key){
        int times = 3;
        String data = null;
        do{
            times--;
            try{
                data = (String) relationship.getProperty(key);
            }catch (Exception e){
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }while(data == null && times > 0);

        return data;
    }
    @Override
    public PathExpander<State> reverse() {
        return null;
    }
}
