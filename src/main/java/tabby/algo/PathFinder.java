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

    @Procedure
    @Description("apoc.algo.tabbyPathFinder(sinkNode, sourceNode, 5) YIELD path, " +
            "weight - run tabbyPathFinder with maxNodes")
    public Stream<PathResult> tabbyPathFinder(
            @Name("sinkNode") Node startNode,
            @Name("sourceNode") Node endNode,
            @Name("maxNodes") long maxNodes) {

        org.neo4j.graphalgo.PathFinder<Path> algo = new AllSimplePathsFinder(
                new BasicEvaluationContext(tx, db),
                TabbyPathExpander.newInstance(),
                (int) maxNodes
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNode);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

}
