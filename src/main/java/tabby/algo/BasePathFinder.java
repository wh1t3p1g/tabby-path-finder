package tabby.algo;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphalgo.impl.path.TraversalPathFinder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;

import static org.neo4j.graphdb.traversal.Evaluators.toDepth;

/**
 * @author wh1t3p1g
 * @since 2022/1/5
 */
public class BasePathFinder extends TraversalPathFinder {

    private final PathExpander expander;
    private final EvaluationContext context;
    private final int maxDepth;
    private final boolean depthFirst;

    public BasePathFinder(EvaluationContext context, PathExpander expander, int maxDepth, boolean depthFirst) {
        this.expander = expander;
        this.context = context;
        this.maxDepth = maxDepth;
        this.depthFirst = depthFirst;
    }

    protected Uniqueness uniqueness()
    {
        return Uniqueness.NODE_PATH;
    }

    @Override
    protected Traverser instantiateTraverser(Node start, Node end) {
        Transaction transaction = context.transaction();
        TraversalDescription base = transaction.traversalDescription()
                                        .uniqueness( uniqueness() );
        if(depthFirst){
            base = base.depthFirst();
        }else{
            base = base.breadthFirst();
        }

        InitialBranchState.State<Object> stack = new InitialBranchState.State<>("", "");

        return transaction.bidirectionalTraversalDescription()
                .startSide( base.expand( expander, stack ).evaluator( toDepth( maxDepth / 2 ) ) )
                .endSide( base.expand( expander.reverse(), stack.reverse() ).evaluator( toDepth( maxDepth - maxDepth / 2 ) ) )
                .traverse( start, end );
    }

}
