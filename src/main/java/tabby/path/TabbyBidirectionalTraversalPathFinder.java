package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import tabby.data.TabbyState;
import tabby.evaluator.CollisionDetector;

import static org.neo4j.graphdb.traversal.Evaluators.toDepth;

/**
 * @author wh1t3p1g
 * @since 2023/8/23
 */
public class TabbyBidirectionalTraversalPathFinder extends BasePathFinder<TabbyState> {

    private InitialBranchState.State<TabbyState> sourceState;
    private InitialBranchState.State<TabbyState> sinkState;

    public TabbyBidirectionalTraversalPathFinder(EvaluationContext context,
                                                 PathExpander<TabbyState> expander,
                                                 TabbyState sourceState,
                                                 TabbyState sinkState, Number maxDepth,
                                                 boolean depthFirst) {
        super(context, expander, maxDepth, depthFirst);
        this.sourceState = new InitialBranchState.State<>(sourceState, TabbyState.of());
        this.sinkState = new InitialBranchState.State<>(sinkState, TabbyState.of());
    }

    @Override
    protected Traverser instantiateTraverser(Node start, Node end) {
        Transaction transaction = context.transaction();
        TraversalDescription base = getBaseDescription(transaction);

        return transaction.bidirectionalTraversalDescription()
                .startSide( base.expand( expander, sourceState ).evaluator( toDepth( (Integer)maxDepth / 2 ) ) )
                .endSide( base.expand( expander.reverse(), sinkState ).evaluator( toDepth( (Integer)maxDepth - (Integer)maxDepth / 2 ) ) )
                .collisionEvaluator(Evaluators.all())
                .collisionPolicy(CollisionDetector::new)
                .traverse( start, end );
    }

    public TraversalDescription getBaseDescription(Transaction transaction){
        TraversalDescription base = transaction.traversalDescription();

        if(depthFirst){
            base = base.depthFirst();
        }else{
            base = base.breadthFirst();
        }

        return base.uniqueness(uniqueness());
    }
}
