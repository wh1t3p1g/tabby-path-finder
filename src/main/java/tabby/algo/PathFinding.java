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
import tabby.evaluator.judgment.CommonJudgment;
import tabby.evaluator.judgment.JavaGadgetJudgment;
import tabby.expander.BackwardPathExpander;
import tabby.expander.ForwardedPathExpander;
import tabby.expander.processor.ProcessorFactory;
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
    @Description("tabby.algo.findVul(sink, sources, maxNodes, state, parallel, depthFirst) YIELD path, " +
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
                new BackwardPathExpander(parallel),
                (int) maxNodes,
                state, depthFirst, null
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.findJavaGadget(sink, sources, maxNodes, state, parallel, depthFirst) YIELD path, " +
            "weight - run findJavaGadget with maxNodes and state")
    public Stream<PathResult> findJavaGadget(
            @Name("sinkNode") Node startNode,
            @Name("sourceNodes") List<Node> endNodes,
            @Name("maxNodes") long maxNodes,
            @Name("state") String state,
            @Name("parallel") boolean parallel,
            @Name("depthFirst") boolean depthFirst) {

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new BackwardPathExpander(parallel),
                (int) maxNodes,
                state, depthFirst, new JavaGadgetJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.findVul(sourceNode, sinkNodes, maxLength, depthFirst) YIELD path, " +
            "weight - run findVul from source node to sink nodes")
    public Stream<PathResult> findVul(
            @Name("startNode") Node startNode,
            @Name("endNodes") List<Node> endNodes,
            @Name("maxLength") long maxLength,
            @Name("depthFirst") boolean depthFirst) {

        long parameterSize = (long) startNode.getProperty("PARAMETER_SIZE", 0);
        int[] initialPositions = new int[(int) (parameterSize+1)];
        initialPositions[0] = -1; // check this
        for(int i=0; i<parameterSize;i++){
            initialPositions[i+1] = i;
        }

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new ForwardedPathExpander(false, ProcessorFactory.newInstance("Common")),
                (int) maxLength, initialPositions,
                depthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

}
