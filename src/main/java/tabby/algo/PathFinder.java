package tabby.algo;

import org.neo4j.graphalgo.BasicEvaluationContext;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import tabby.expander.BidirectionalPathExpander;
import tabby.path.BidirectionalTraversalPathFinder;
import tabby.result.PathResult;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class PathFinder {

    @Context
    public GraphDatabaseService db;

    @Context
    public Transaction tx;

    /**
     * 依赖call边上的PROPAGATED
     * 当propagated为true时，当前节点可扩展call、alias
     * 当propagated为false时，当前节点仅可扩展call
     * 实际情况，当前图为过程内分析后的图时，很少为false的情况，但是也能减少一部分的查询工作
     * @param startNode
     * @param endNode
     * @param maxNodes
     * @return
     */
    @Procedure
    @Description("tabby.algo.allSimplePaths(sinkNode, sourceNode, 5, true) YIELD path, " +
            "weight - run allSimplePaths with maxNodes and depthFirst")
    public Stream<PathResult> allSimplePaths(
            @Name("sinkNode") Node startNode,
            @Name("sourceNode") Node endNode,
            @Name("maxNodes") long maxNodes,
            @Name("depthFirst") boolean depthFirst) {

        org.neo4j.graphalgo.PathFinder<Path> algo = new BidirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                BidirectionalPathExpander.newInstance(),
                (int) maxNodes,
                depthFirst
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNode);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

}
