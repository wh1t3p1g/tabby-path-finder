package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.Traverser;
import tabby.evaluator.MonoPathEvaluator;
import tabby.util.JsonHelper;
import tabby.util.State;

/**
 * @author wh1t3p1g
 * @since 2022/4/26
 */
public class MonoDirectionalTraversalPathFinder extends BasePathFinder{

    private int[] initialPositions;

    public MonoDirectionalTraversalPathFinder(EvaluationContext context,
                                              PathExpander expander,
                                              int maxDepth,
                                              String state
    ) {
        super(context, expander, maxDepth, true);
        if(state != null){
            init(state);
        }
    }

    @Override
    protected Traverser instantiateTraverser(Node start, Node end) {
        Transaction transaction = context.transaction();

        if(initialPositions == null){
            String position = (String) start.getProperty("POLLUTED_POSITION", "[]");
            init(position);
        }

        InitialBranchState.State<State> stack
                = new InitialBranchState.State<>(
                        State.newInstance(initialPositions),
                        State.newInstance(initialPositions));

        return transaction.traversalDescription()
                .depthFirst()
                .expand(expander, stack)
                .evaluator(MonoPathEvaluator.of(end, maxDepth))
                .uniqueness(uniqueness())
                .traverse( start, end );
    }

    public void init(String pp){
        if(initialPositions == null){
            initialPositions = JsonHelper.parsePollutedPosition(pp);
        }
    }
}
