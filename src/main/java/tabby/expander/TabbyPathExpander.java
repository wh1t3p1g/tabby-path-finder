package tabby.expander;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.internal.helpers.collection.Iterables;
import tabby.data.TabbyState;
import tabby.expander.processor.BackwardedProcessor;
import tabby.expander.processor.ForwardedProcessor;
import tabby.expander.processor.Processor;
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
public class TabbyPathExpander implements PathExpander<TabbyState> {

    private final Direction direction;
    private final RelationshipType[] relationshipTypes;
    private boolean parallel = false;
    private boolean isBackward = false;
    private Processor<TabbyState> processor;

    public TabbyPathExpander(boolean parallel, boolean isBackward) {
        String[] types;

        this.parallel = parallel;
        this.isBackward = isBackward;

        if(isBackward){
            this.processor = new BackwardedProcessor();
            types = new String[]{"<CALL", "<ALIAS"};
        }else{
            this.processor = new ForwardedProcessor();
            types = new String[]{"CALL>", "ALIAS>"};
        }

        direction = Types.directionFor(types[0]);
        relationshipTypes = new RelationshipType[]{
                    Types.relationshipTypeFor(types[0]),
                    Types.relationshipTypeFor(types[1])
                };
    }

    @Override
    public ResourceIterable<Relationship> expand(Path path, BranchState<TabbyState> state) {
        final Node node = path.endNode();
        final Relationship lastRelationship = path.lastRelationship();
        processor.init(node, state.getState(), lastRelationship);

        if(processor.isNeedProcess()){
            Iterable<Relationship> relationships = node.getRelationships(direction, relationshipTypes);
            List<Relationship> nextRelationships = StreamSupport.stream(relationships.spliterator(), parallel)
                    .map((next) -> processor.process(next))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            state.setState(processor.getNextState());
            return Iterables.asResourceIterable(nextRelationships);
        }

        return Iterables.asResourceIterable(new ArrayList<>());
    }

    @Override
    public PathExpander<TabbyState> reverse() {
        return new TabbyPathExpander(parallel, !isBackward);
    }
}
