package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.traversal.Traverser;
import tabby.data.State;

import java.util.List;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
@Deprecated
public class BidirectionalTraversalPathFinder extends BasePathFinder{

    public BidirectionalTraversalPathFinder(EvaluationContext context, PathExpander<State> expander, int maxDepth, boolean depthFirst) {
        super(context, expander, maxDepth, depthFirst);
    }

    @Override
    protected Traverser instantiateTraverser(Node start, List<Node> ends) {
        return null;
    }

    @Override
    protected Traverser instantiateTraverser(List<Node> starts, List<Node> ends) {
        return null;
    }

    @Override
    protected Traverser instantiateTraverser(Node start, Node end) {
//        Transaction transaction = context.transaction();
//        TraversalDescription base = transaction.traversalDescription()
//                .uniqueness( uniqueness() );
//
//        if(depthFirst){
//            base = base.depthFirst();
//        }else{
//            base = base.breadthFirst();
//        }
//
//        InitialBranchState.State<Object> stack = new InitialBranchState.State<>("", "");
//
//        return transaction.bidirectionalTraversalDescription()
//                .startSide( base.expand( expander, stack ).evaluator( toDepth( maxDepth / 2 ) ) )
//                .endSide( base.expand( expander.reverse(), stack.reverse() ).evaluator( toDepth( maxDepth - maxDepth / 2 ) ) )
//                .traverse( start, end );
        return null;
    }
}
