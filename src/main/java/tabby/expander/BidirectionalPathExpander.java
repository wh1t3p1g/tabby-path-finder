package tabby.expander;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.internal.helpers.collection.Pair;
import tabby.predicate.LowPredicate;
import tabby.util.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        boolean callInitialed = false;
        final Relationship relationship = path.lastRelationship();
        List<Relationship> nextRelationships = new ArrayList<>();
        if(relationship != null){
            RelationshipType type = relationship.getType();
            boolean isCall = "CALL".equals(type.name());
            if(order < 0 && isCall){// 从source -> sink
                String nodeType = (String) node.getProperty("CLASSNAME", "");
                propagated = new LowPredicate(nodeType).test(relationship);
            }

            if(order > 0 && !isCall){// 从sink -> source
                // 当lastRelationship为alias边时，大概率当前endNode为接口类型，所以判断所有call到endNode的边是否是可传播的
                nextRelationships.addAll(findNextCallEdges(node, true));
                callInitialed = true;
            }
        }

        if(!callInitialed){
            nextRelationships.addAll(findNextCallEdges(node, false));
        }

        if(propagated){// 可传播时，CALL ALIAS均可扩展
            nextRelationships.addAll(findNextAliasEdges(node));
        }

        return asResourceIterator(nextRelationships);
    }

    public List<Relationship> findNextCallEdges(Node node, boolean check){
        DirectionAndTypes call = getDirectionAndTypes("CALL");
        Iterable<Relationship> relationships = node.getRelationships(call.direction, call.types);
        String nodeType = (String) node.getProperty("CLASSNAME", "");
        if(check){
            return StreamSupport.stream(relationships.spliterator(), true)
                    .filter(new LowPredicate(nodeType))
                    .collect(Collectors.toList());
        }else{
            return StreamSupport.stream(relationships.spliterator(), true)
                    .collect(Collectors.toList());
        }
    }

    public List<Relationship> findNextAliasEdges(Node node){
        DirectionAndTypes alias = getDirectionAndTypes("ALIAS");
        Iterable<Relationship> relationships = node.getRelationships(alias.direction, alias.types);

        return StreamSupport.stream(relationships.spliterator(), true)
                .collect(Collectors.toList());
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
