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
import tabby.util.JsonHelper;

import java.util.Collections;
import java.util.List;

/**
 * @author wh1t3p1g
 * @since 2022/4/26
 */
public class MonoDirectionalTraversalPathFinder extends BasePathFinder{

    private InitialBranchState.State<State> stack;

    public MonoDirectionalTraversalPathFinder(EvaluationContext context,
                                              PathExpander<State> expander,
                                              int maxDepth,
                                              State state,
                                              boolean depthFirst
    ) {
        super(context, expander, maxDepth, depthFirst);
        this.stack = new InitialBranchState.State<>(state, state.copy());
    }

    @Override
    protected Traverser instantiateTraverser(Node start, Node end) {
        Transaction transaction = context.transaction();

        TraversalDescription base = getBaseDescription(transaction, Collections.singletonList(start));

        return base.expand(expander, stack).evaluator(MonoPathEvaluator.of(end, maxDepth)).traverse(start);
    }

    @Override
    protected Traverser instantiateTraverser(Node start, List<Node> ends) {
        Transaction transaction = context.transaction();

        TraversalDescription base = getBaseDescription(transaction, Collections.singletonList(start));

        return base.expand(expander, stack).evaluator(MultiMonoPathEvaluator.of(ends, maxDepth)).traverse(start);
    }

    @Override
    protected Traverser instantiateTraverser(List<Node> starts, List<Node> ends) {
        Transaction transaction = context.transaction();

        TraversalDescription base = getBaseDescription(transaction, starts);

        return base.expand(expander, stack).evaluator(MultiMonoPathEvaluator.of(ends, maxDepth)).traverse(starts);
    }

    public TraversalDescription getBaseDescription(Transaction transaction, List<Node> starts){
        initialStack(starts);

        TraversalDescription base = transaction.traversalDescription();
        if(depthFirst){
            base = base.depthFirst();
        }else{
            base = base.breadthFirst();
        }

        return base.uniqueness(uniqueness());
    }

    public void initialStack(List<Node> starts){
        if(stack == null){
            State state = State.newInstance();
            for(Node start:starts){
                String position = (String) start.getProperty("POLLUTED_POSITION", "[]");
                int[][] initialPositions = JsonHelper.parse(position);
                state.addInitialPositions(start.getId(), initialPositions);
            }
            stack = new InitialBranchState.State<>(state, state.copy());
        }
    }
}
