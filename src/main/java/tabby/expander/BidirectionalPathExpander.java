package tabby.expander;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.internal.helpers.collection.ArrayIterator;
import org.neo4j.internal.helpers.collection.NestingResourceIterator;
import org.neo4j.internal.helpers.collection.Pair;
import tabby.util.Types;

import java.util.HashMap;
import java.util.Map;

import static org.neo4j.internal.helpers.collection.Iterators.asResourceIterator;

/**
 * @author wh1t3p1g
 * @since 2022/1/5
 */
public class BidirectionalPathExpander extends BasePathExpander.RegularExpander {


    BidirectionalPathExpander(Map<Direction, RelationshipType[]> types) {
        super(types);
    }

    @Override
    ResourceIterator<Relationship> doExpand(Path path, BranchState state) {
        final Node node = path.endNode();
        boolean propagated = true;

        if(order < 0){
            final Relationship relationship = path.lastRelationship();
            if(relationship != null && relationship.isType(RelationshipType.withName("CALL"))){
                propagated = (boolean) relationship.getProperty("PROPAGATED", true);
            }
        }

        if(propagated){// 可传播时，CALL ALIAS均可扩展
            return new NestingResourceIterator<>( new ArrayIterator<>( directions ) )
            {
                @Override
                protected ResourceIterator<Relationship> createNestedIterator( DirectionAndTypes item )
                {
                    return asResourceIterator( node.getRelationships( item.direction, item.types ) );
                }
            };
        }else{// 不可传播时，CALL可扩展
            DirectionAndTypes call = getDirectionAndTypes("CALL");
            return asResourceIterator( node.getRelationships( call.direction, call.types ) );
        }
    }

    @Override
    BasePathExpander createNew(Map<Direction, RelationshipType[]> types) {
        if(types == null){
            types = new HashMap<>();
        }
        return new BidirectionalPathExpander( types );
    }

    @Override
    public BasePathExpander reverse() {
        BasePathExpander temp = super.reverse();
        temp.order = -1 * order;
        return temp;
    }

    public DirectionAndTypes getDirectionAndTypes(String typename){
        RelationshipType t = Types.relationshipTypeFor(typename);
        for(DirectionAndTypes dt:directions){
            for(RelationshipType type:dt.types){
                if(t != null && t.equals(type)){
                    return dt;
                }
            }
        }
        return null;
    }

    public static BasePathExpander newInstance() {
        Map<Direction, RelationshipType[]> types = new HashMap<>();
        BasePathExpander tabbyPathExpander = new BidirectionalPathExpander(types);
        for (Pair<RelationshipType, Direction> pair : Types.parse("<CALL|ALIAS")) {
            if (pair.other() == null) {
                tabbyPathExpander = tabbyPathExpander.add(pair.first());
            } else {
                tabbyPathExpander = tabbyPathExpander.add(pair.first(), pair.other());
            }
        }
        return tabbyPathExpander;
    }
}
