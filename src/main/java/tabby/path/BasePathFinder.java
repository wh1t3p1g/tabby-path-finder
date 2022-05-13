package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphalgo.impl.path.TraversalPathFinder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.internal.helpers.collection.LimitingIterable;
import tabby.data.State;

import java.util.List;

/**
 * @author wh1t3p1g
 * @since 2022/1/5
 */
public abstract class BasePathFinder extends TraversalPathFinder {

    public final PathExpander<State> expander;
    public final EvaluationContext context;
    public final int maxDepth;
    public final boolean depthFirst;
    private Traverser lastTraverser;

    public BasePathFinder(EvaluationContext context, PathExpander<State> expander, int maxDepth, boolean depthFirst) {
        this.expander = expander;
        this.context = context;
        this.maxDepth = maxDepth;
        this.depthFirst = depthFirst;
    }

    protected Uniqueness uniqueness()
    {
        // 从边的角度，会非常全，但相应的也会增加分析时间
        // 从node的角度，会丢失相同节点的另一种通路，但是对漏洞挖掘来说可接受？
//        return Uniqueness.RELATIONSHIP_PATH;
        return Uniqueness.NODE_PATH;
    }

    public Iterable<Path> findAllPaths(Node start, List<Node> ends){
        lastTraverser = instantiateTraverser( start, ends );
        Integer maxResultCount = maxResultCount();
        return maxResultCount != null ? new LimitingIterable<>( lastTraverser, maxResultCount ) : lastTraverser;
    }

    public Iterable<Path> findAllPaths(List<Node> starts, List<Node> ends){
        lastTraverser = instantiateTraverser( starts, ends );
        Integer maxResultCount = maxResultCount();
        return maxResultCount != null ? new LimitingIterable<>( lastTraverser, maxResultCount ) : lastTraverser;
    }

    protected abstract Traverser instantiateTraverser(Node start, List<Node> ends );

    protected abstract Traverser instantiateTraverser(List<Node> starts, List<Node> ends );
}
