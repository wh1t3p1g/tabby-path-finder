package tabby.expander;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import tabby.calculator.Calculator;
import tabby.calculator.ForwardedCalculator;
import tabby.expander.processor.Processor;
import tabby.data.State;
import tabby.util.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * from source to sink
 * @author wh1t3p1g
 * @since 2022/4/26
 */
public class ForwardedPathExpander implements PathExpander<State> {

    private final Direction direction;
    private final RelationshipType[] relationshipTypes;
    private boolean parallel = false;
    private Processor processor;
    private Calculator calculator;

    public ForwardedPathExpander(boolean parallel, Processor processor) {
        this.processor = processor;
        String[] types = new String[]{"CALL>", "ALIAS>"};
        direction = Types.directionFor(types[0]);
        relationshipTypes
                = new RelationshipType[]{
                    Types.relationshipTypeFor(types[0]),
                    Types.relationshipTypeFor(types[1])
                };
        this.parallel = parallel;
        this.calculator = new ForwardedCalculator();
    }

    public RelationshipType[] getRelationshipTypes(Relationship relationship, State state){
        // 判断下一个节点是否允许扩展alias边
//        if(relationship != null && state.getNextAlias().contains(relationship.getId())){
//            return new RelationshipType[]{
//                    Types.relationshipTypeFor("CALL>"),
//            };
//        }
        return relationshipTypes;
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<State> state) {
        final Node node = path.endNode();
        final Relationship lastRelationship = path.lastRelationship();
        processor.init(node, state.getState(), lastRelationship, calculator);

        if(processor.isNeedProcess()){
            Iterable<Relationship> relationships = node.getRelationships(direction, getRelationshipTypes(lastRelationship, state.getState()));
            List<Relationship> nextRelationships = StreamSupport.stream(relationships.spliterator(), parallel)
                    .map((next) -> processor.process(next))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            state.setState(processor.getNextState());
            return nextRelationships;
        }

        return new ArrayList<>();
    }

    @Override
    public PathExpander<State> reverse() {
        return null;
    }
}
