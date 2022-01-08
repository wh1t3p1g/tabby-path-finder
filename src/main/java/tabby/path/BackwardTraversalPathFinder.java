package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.PathEvaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import tabby.evaluator.BackwardPathEvaluator;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class BackwardTraversalPathFinder extends BasePathFinder{


    public BackwardTraversalPathFinder(EvaluationContext context, PathExpander expander, int maxDepth, boolean depthFirst) {
        super(context, expander, maxDepth, depthFirst);
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

        InitialBranchState.State<Object> stack = new InitialBranchState.State<>("", "");
        PathEvaluator evaluator = new BackwardPathEvaluator((String) end.getProperty("SIGNATURE", ""), maxDepth);

        return base.expand(expander, stack)
                   .evaluator(evaluator)
                   .traverse(start);
    }
}
