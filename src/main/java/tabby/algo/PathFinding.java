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
import tabby.expander.MonoDirectionalPathExpander;
import tabby.path.MonoDirectionalTraversalPathFinder;
import tabby.result.PathResult;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class PathFinding {

    @Context
    public GraphDatabaseService db;

    @Context
    public Transaction tx;

    @Procedure
    @Description("tabby.algo.allSimplePaths(sink, sources, maxNodes, parallel, depthFirst) YIELD path, " +
            "weight - run allSimplePaths with maxNodes")
    public Stream<PathResult> allSimplePaths(
            @Name("sinkNode") Node startNode,
            @Name("sourceNode") List<Node> endNodes,
            @Name("maxNodes") long maxNodes,
            @Name("parallel") boolean parallel,
            @Name("depthFirst") boolean depthFirst) {

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new MonoDirectionalPathExpander(parallel),
                (int) maxNodes, null, depthFirst
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.allSimplePathsWithState(sink, sources, maxNodes, state, parallel, depthFirst) YIELD path, " +
            "weight - run allSimplePathsWithState with maxNodes and state")
    public Stream<PathResult> allSimplePathsWithState(
            @Name("sinkNode") Node startNode,
            @Name("sourceNodes") List<Node> endNodes,
            @Name("maxNodes") long maxNodes,
            @Name("state") String state,
            @Name("parallel") boolean parallel,
            @Name("depthFirst") boolean depthFirst) {

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new MonoDirectionalPathExpander(parallel),
                (int) maxNodes,
                state, depthFirst
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

}
