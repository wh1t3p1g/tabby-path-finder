package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphalgo.impl.path.TraversalPathFinder;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.traversal.Uniqueness;

/**
 * @author wh1t3p1g
 * @since 2022/1/5
 */
public abstract class BasePathFinder extends TraversalPathFinder {

    public final PathExpander expander;
    public final EvaluationContext context;
    public final int maxDepth;
    public final boolean depthFirst;

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
}
