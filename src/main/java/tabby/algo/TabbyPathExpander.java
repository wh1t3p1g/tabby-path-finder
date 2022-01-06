package tabby.algo;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.internal.helpers.collection.ArrayIterator;
import org.neo4j.internal.helpers.collection.NestingResourceIterator;
import org.neo4j.internal.helpers.collection.Pair;
import tabby.path.RelationshipTypeAndDirections;

import java.util.HashMap;
import java.util.Map;

import static org.neo4j.internal.helpers.collection.Iterators.asResourceIterator;

/**
 * @author wh1t3p1g
 * @since 2022/1/5
 */
public class TabbyPathExpander extends AllSimplePathsExpander.RegularExpander {

    TabbyPathExpander(Map<Direction, RelationshipType[]> types) {
        super(types);
    }


    @Override
    ResourceIterator<Relationship> doExpand(Path path, BranchState state) {
        final Node node = path.endNode();
        final Relationship relationship = path.lastRelationship();
        boolean propagated = true;
        if(relationship != null){
            propagated = (boolean) relationship.getProperty("PROPAGATED", true);
//            if(relationship.isType(RelationshipType.withName("CALL"))){
//                state.setState(node.getProperty("POLLUTED_POSITION", new int[0]));
//            }else{
//                state.setState(state.getState());
//            }
            // 这里调整一下，可以用来做状态传递
        }

        if(propagated){
            return new NestingResourceIterator<>( new ArrayIterator<>( directions ) )
            {
                @Override
                protected ResourceIterator<Relationship> createNestedIterator( DirectionAndTypes item )
                {
                    return asResourceIterator( node.getRelationships( item.direction, item.types ) );
                }
            };
        }else{
            DirectionAndTypes call = getDirectionAndTypes("CALL");
            return asResourceIterator( node.getRelationships( call.direction, call.types ) );
        }
    }

    @Override
    AllSimplePathsExpander createNew(Map<Direction, RelationshipType[]> types) {
        if(types == null){
            types = new HashMap<>();
        }
        return new TabbyPathExpander( types );
    }

    public DirectionAndTypes getDirectionAndTypes(String typename){
        RelationshipType t = RelationshipTypeAndDirections.relationshipTypeFor(typename);
        for(DirectionAndTypes dt:directions){
            for(RelationshipType type:dt.types){
                if(t != null && t.equals(type)){
                    return dt;
                }
            }
        }
        return null;
    }

    public static AllSimplePathsExpander newInstance() {
        Map<Direction, RelationshipType[]> types = new HashMap<>();
        AllSimplePathsExpander tabbyPathExpander = new TabbyPathExpander(types);
        for (Pair<RelationshipType, Direction> pair : RelationshipTypeAndDirections
                .parse("<CALL|ALIAS")) {
            if (pair.other() == null) {
                tabbyPathExpander = tabbyPathExpander.add(pair.first());
            } else {
                tabbyPathExpander = tabbyPathExpander.add(pair.first(), pair.other());
            }
        }
        return tabbyPathExpander;
    }
}
