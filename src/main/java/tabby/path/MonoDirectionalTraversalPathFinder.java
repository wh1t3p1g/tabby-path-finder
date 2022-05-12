package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import tabby.data.State;
import tabby.evaluator.MonoPathEvaluator;
import tabby.evaluator.MultiMonoPathEvaluator;
import tabby.evaluator.judgment.Judgment;
import tabby.util.JsonHelper;

import java.util.List;

/**
 * @author wh1t3p1g
 * @since 2022/4/26
 */
public class MonoDirectionalTraversalPathFinder extends BasePathFinder{

    private int[] initialPositions;
    private InitialBranchState.State<State> stack;
    private Judgment judgment;

    public MonoDirectionalTraversalPathFinder(EvaluationContext context,
                                              PathExpander<State> expander,
                                              int maxDepth,
                                              String state,
                                              boolean depthFirst,
                                              Judgment judgment
    ) {
        super(context, expander, maxDepth, depthFirst);

        this.judgment = judgment;
        if(state != null){
            init(state);
        }
    }

    public MonoDirectionalTraversalPathFinder(EvaluationContext context,
                                              PathExpander<State> expander,
                                              int maxDepth,
                                              int[] initialPositions,
                                              boolean depthFirst,
                                              Judgment judgment
    ) {
        super(context, expander, maxDepth, depthFirst);

        this.judgment = judgment;
        this.initialPositions = initialPositions;
        this.stack = new InitialBranchState.State<>(
                State.newInstance(initialPositions),
                State.newInstance(initialPositions));
    }

    @Override
    protected Traverser instantiateTraverser(Node start, Node end) {
        Transaction transaction = context.transaction();

        if(initialPositions == null){
            String position = (String) start.getProperty("POLLUTED_POSITION", "[]");
            init(position);
        }
        TraversalDescription base = transaction.traversalDescription();
        if(depthFirst){
            base = base.depthFirst();
        }else{
            base = base.breadthFirst();
        }
        return base.expand(expander, stack)
                .evaluator(MonoPathEvaluator.of(end, maxDepth))
                .uniqueness(uniqueness())
                .traverse(start);
    }

    public void init(String pp){
        if(initialPositions == null){
            initialPositions = JsonHelper.parsePollutedPosition(pp);
            stack = new InitialBranchState.State<>(
                    State.newInstance(initialPositions),
                    State.newInstance(initialPositions));
        }
    }

    @Override
    protected Traverser instantiateTraverser(Node start, List<Node> ends) {
        Transaction transaction = context.transaction();

        if(initialPositions == null){
            String position = (String) start.getProperty("POLLUTED_POSITION", "[]");
            init(position);
        }

        TraversalDescription base = transaction.traversalDescription();
        if(depthFirst){
            base = base.depthFirst();
        }else{
            base = base.breadthFirst();
        }

        return base.expand(expander, stack)
                .evaluator(MultiMonoPathEvaluator.of(ends, maxDepth, judgment))
                .uniqueness(uniqueness())
                .traverse(start);
    }
}
