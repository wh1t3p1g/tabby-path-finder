package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.InitialBranchState;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import tabby.data.TabbyState;
import tabby.evaluator.TabbyEvaluator;

/**
 * @author wh1t3p1g
 * @since 2023/8/23
 */
public class TabbyTraversalPathFinder extends BasePathFinder<TabbyState> {

    private InitialBranchState.State<TabbyState> state;
    private boolean checkAuth = false;
    private boolean isBackward = false;
    private TabbyState endState = null;

    public TabbyTraversalPathFinder(EvaluationContext context,
                                    PathExpander<TabbyState> expander,
                                    TabbyState initialState,
                                    TabbyState endState, Number maxDepth,
                                    boolean depthFirst, boolean checkAuth, boolean isBackward) {
        super(context, expander, maxDepth, depthFirst);
        this.state = new InitialBranchState.State<>(initialState, TabbyState.of());
        this.checkAuth = checkAuth;
        this.isBackward = isBackward;
        this.endState = endState;
    }

    @Override
    protected Traverser instantiateTraverser(Node start, Node end) {
        Transaction transaction = context.transaction();

        TraversalDescription base = getBaseDescription(transaction);

        return base.expand(expander, state)
                .evaluator(TabbyEvaluator.of(end, endState, maxDepth, checkAuth, isBackward))
                .traverse(start);
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
