package tabby.path;

import org.neo4j.graphalgo.EvaluationContext;
import org.neo4j.graphalgo.impl.path.TraversalPathFinder;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.traversal.Uniqueness;

/**
 * @author wh1t3p1g
 * @since 2022/1/5
 */
public abstract class BasePathFinder<STATE> extends TraversalPathFinder {

    public final PathExpander<STATE> expander;
    public final EvaluationContext context;
    public final int maxDepth;
    public final boolean depthFirst;

    public BasePathFinder(EvaluationContext context, PathExpander<STATE> expander, int maxDepth, boolean depthFirst) {
        this.expander = expander;
        this.context = context;
        this.maxDepth = maxDepth;
        this.depthFirst = depthFirst;
    }

    protected Uniqueness uniqueness()
    {
        // 从边的角度，会非常全，但相应的也会增加分析时间
        // 从node的角度，会丢失相同节点的另一种通路，但是对漏洞挖掘来说可接受？不可接受！
        return Uniqueness.RELATIONSHIP_PATH;
    }
}
