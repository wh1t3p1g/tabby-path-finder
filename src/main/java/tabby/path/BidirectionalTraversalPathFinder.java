package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import tabby.data.State;

import static org.neo4j.graphdb.traversal.Evaluators.toDepth;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class BidirectionalTraversalPathFinder extends BasePathFinder<State>{

    private InitialBranchState.State<State> sourceState;
    private InitialBranchState.State<State> sinkState;

    public BidirectionalTraversalPathFinder(EvaluationContext context,
                                            PathExpander<State> expander,
                                            Number maxDepth,
                                            State sourceState,
                                            State sinkState,
                                            boolean depthFirst) {
        super(context, expander, maxDepth, depthFirst);
        this.sourceState = new InitialBranchState.State<>(sourceState, sourceState.copy());
        this.sinkState = new InitialBranchState.State<>(sinkState, sinkState.copy());
    }

    @Override
    protected Traverser instantiateTraverser(Node start, Node end) {
        Transaction transaction = context.transaction();
        TraversalDescription base = transaction.traversalDescription().uniqueness( uniqueness() );

        if(depthFirst){
            base = base.depthFirst();
        }else{
            base = base.breadthFirst();
        }

        return transaction.bidirectionalTraversalDescription()
                .startSide( base.expand( expander, sourceState ).evaluator( toDepth( (Integer)maxDepth / 2 ) ) )
                .endSide( base.expand( expander.reverse(), sinkState ).evaluator( toDepth( (Integer)maxDepth - (Integer)maxDepth / 2 ) ) )
                .traverse( start, end );
    }
}
