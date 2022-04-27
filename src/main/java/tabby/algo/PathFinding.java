package tabby.algo;

import org.neo4j.graphalgo.BasicEvaluationContext;
import org.neo4j.graphalgo.PathFinder;
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
    @Description("tabby.algo.allPaths(sink, source, 5) YIELD path, " +
            "weight - run allPaths with maxNodes and depthFirst, depthFirst")
    public Stream<PathResult> allPaths(
            @Name("sinkNode") Node startNode,
            @Name("sourceNode") Node endNode,
            @Name("maxNodes") long maxNodes) {

        PathFinder<Path> algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new MonoDirectionalPathExpander(),
                (int) maxNodes, null
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNode);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.allPaths(sink, source, 5) YIELD path, " +
            "weight - run allPaths with maxNodes and depthFirst, depthFirst")
    public Stream<PathResult> allPathsWithState(
            @Name("sinkNode") Node startNode,
            @Name("sourceNode") Node endNode,
            @Name("maxNodes") long maxNodes,
            @Name("state") String state) {

        PathFinder<Path> algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new MonoDirectionalPathExpander(),
                (int) maxNodes,
                state
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNode);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

}
