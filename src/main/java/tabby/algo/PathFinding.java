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
import tabby.data.State;
import tabby.evaluator.judgment.CommonJudgment;
import tabby.expander.BackwardPathExpander;
import tabby.expander.ForwardedPathExpander;
import tabby.expander.processor.ProcessorFactory;
import tabby.path.MonoDirectionalTraversalPathFinder;
import tabby.result.PathResult;
import tabby.util.JsonHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    @Description("tabby.algo.allSimplePath(sink, sources, maxNodes, state, depthFirst) YIELD path, " +
            "weight - run allSimplePath with maxNodes and state")
    public Stream<PathResult> allSimplePath(
            @Name("sinkNode") Node startNode,
            @Name("sourceNodes") List<Node> endNodes,
            @Name("maxNodes") long maxNodes,
            @Name("state") String state,
            @Name("depthFirst") boolean depthFirst) {

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new BackwardPathExpander(false),
                (int) maxNodes, getInitialState(Collections.singletonList(startNode), state),
                depthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.allSimplePaths(sinks, sources, maxNodes, depthFirst) YIELD path, " +
            "weight - run allSimplePaths with maxNodes and state")
    public Stream<PathResult> allSimplePaths(
            @Name("sinkNodes") List<Node> startNodes,
            @Name("sourceNodes") List<Node> endNodes,
            @Name("maxNodes") long maxNodes,
            @Name("depthFirst") boolean depthFirst) {

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new BackwardPathExpander(false),
                (int) maxNodes, getInitialState(startNodes, null),
                depthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNodes, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.findJavaGadget(source, sinks, maxNodes, depthFirst) YIELD path, " +
            "weight - run findJavaGadget with maxNodes from source to sink")
    public Stream<PathResult> findJavaGadget(
            @Name("startNode") Node startNode,
            @Name("endNodes") List<Node> endNodes,
            @Name("maxLength") long maxLength,
            @Name("depthFirst") boolean depthFirst) {

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new ForwardedPathExpander(false, ProcessorFactory.newInstance("JavaGadget")),
                (int) maxLength, getInitialState(Collections.singletonList(startNode)),
                depthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.findAllJavaGadget(sources, sinks, maxNodes, depthFirst) YIELD path, " +
            "weight - run findAllJavaGadget with maxNodes from source to sink")
    public Stream<PathResult> findAllJavaGadget(
            @Name("startNodes") List<Node> startNodes,
            @Name("endNodes") List<Node> endNodes,
            @Name("maxLength") long maxLength,
            @Name("depthFirst") boolean depthFirst) {

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new ForwardedPathExpander(false, ProcessorFactory.newInstance("JavaGadget")),
                (int) maxLength, getInitialState(startNodes),
                depthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNodes, endNodes);
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

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new ForwardedPathExpander(false, ProcessorFactory.newInstance("Common")),
                (int) maxLength, getInitialState(Collections.singletonList(startNode)),
                depthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.findAllVul(sourceNodes, sinkNodes, maxLength, depthFirst) YIELD path, " +
            "weight - run findAllVul from source node to sink nodes")
    public Stream<PathResult> findAllVul(
            @Name("startNode") List<Node> startNodes,
            @Name("endNodes") List<Node> endNodes,
            @Name("maxLength") long maxLength,
            @Name("depthFirst") boolean depthFirst) {

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                new ForwardedPathExpander(false, ProcessorFactory.newInstance("Common")),
                (int) maxLength, getInitialState(startNodes),
                depthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNodes, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    public State getInitialState(List<Node> nodes){
        State state = State.newInstance();
        for(Node node:nodes){
            long parameterSize = (long) node.getProperty("PARAMETER_SIZE", 0);
            int[] initialPositions = new int[(int) (parameterSize+1)];
            initialPositions[0] = -1; // check this
            for(int i=0; i<parameterSize;i++){
                initialPositions[i+1] = i;
            }
            state.addInitialPositions(node.getId(), initialPositions);
        }

        return state;
    }

    public State getInitialState(List<Node> nodes, String polluted){
        State state = State.newInstance();
        int[] initialPositions = null;
        if(polluted != null){
            initialPositions = JsonHelper.parsePollutedPosition(polluted);
        }

        for(Node node:nodes){
            if(initialPositions == null){
                Map<String, Object> properties = node.getProperties("IS_SINK", "POLLUTED_POSITION");
                boolean isSink = (boolean) properties.getOrDefault("IS_SINK", false);
                if(isSink){
                    polluted = (String) properties.getOrDefault("POLLUTED_POSITION", "[]");
                    state.addInitialPositions(node.getId(), JsonHelper.parsePollutedPosition(polluted));
                }
            }else{
                state.addInitialPositions(node.getId(), initialPositions);
            }
        }

        return state;
    }

}
