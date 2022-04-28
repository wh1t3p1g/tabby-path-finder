package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphalgo.impl.path.TraversalPathFinder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.internal.helpers.collection.LimitingIterable;

import java.util.List;

/**
 * @author wh1t3p1g
 * @since 2022/1/5
 */
public abstract class BasePathFinder extends TraversalPathFinder {

    public final PathExpander expander;
    public final EvaluationContext context;
    public final int maxDepth;
    public final boolean depthFirst;
    private Traverser lastTraverser;

    public BasePathFinder(EvaluationContext context, PathExpander expander, int maxDepth, boolean depthFirst) {
        this.expander = expander;
        this.context = context;
        this.maxDepth = maxDepth;
        this.depthFirst = depthFirst;
    }

    protected Uniqueness uniqueness()
    {
        return Uniqueness.RELATIONSHIP_PATH;
    }

    public Iterable<Path> findAllPaths(Node start, List<Node> ends){
        lastTraverser = instantiateTraverser( start, ends );
        Integer maxResultCount = maxResultCount();
        return maxResultCount != null ? new LimitingIterable<>( lastTraverser, maxResultCount ) : lastTraverser;
    }

    protected abstract Traverser instantiateTraverser(Node start, List<Node> ends );
}
