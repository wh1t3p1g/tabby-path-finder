package tabby.algo;

import org.neo4j.graphalgo.BasicEvaluationContext;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import tabby.data.State;
import tabby.evaluator.judgment.CommonJudgment;
import tabby.expander.SimplePathExpander;
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
    @Description("tabby.algo.findPath(startNode, endNodes, maxNodeLength, isBackward, isDepthFirst) YIELD path, weight" +
            " - using findPath to get source-sinks path or sink-sources path with maxNodeLength")
    public Stream<PathResult> findPath(@Name("startNode") Node startNode,
                                       @Name("endNodes") List<Node> endNodes,
                                       @Name("maxNodeLength") long maxNodeLength,
                                       @Name("isBackward") boolean isBackward,
                                       @Name("isDepthFirst") boolean isDepthFirst){
        State initialState;

        if(isBackward){
            initialState = getInitialState(Collections.singletonList(startNode), null);
        }else{
            initialState = getInitialState(Collections.singletonList(startNode));
        }

        PathExpander<State> expander = new SimplePathExpander(
                ProcessorFactory.newInstance("Common"), false, isBackward);

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                expander, (int) maxNodeLength, initialState, isDepthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    /**
     * 后向分析，需提供 sink 函数的 污点数据
     * 不提供前向分析，即source to sink
     * @param startNode
     * @param endNodes
     * @param maxNodeLength
     * @param state
     * @param isDepthFirst
     * @return
     */
    @Procedure
    @Description("tabby.algo.findPathWithState(startNode, endNodes, maxNodeLength, state, isDepthFirst) YIELD path, weight" +
            " - using findPath to get sink-sources path with maxNodeLength and state")
    public Stream<PathResult> findPathWithState(@Name("startNode") Node startNode,
                                                @Name("endNodes") List<Node> endNodes,
                                                @Name("maxNodeLength") long maxNodeLength,
                                                @Name("state") String state,
                                                @Name("isDepthFirst") boolean isDepthFirst){
        PathExpander<State> expander = new SimplePathExpander(
                ProcessorFactory.newInstance("Common"), false, true);
        State initialState = getInitialState(Collections.singletonList(startNode), state);

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                expander, (int) maxNodeLength, initialState, isDepthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.findAllPaths(startNodes, endNodes, maxNodeLength, isBackward, isDepthFirst) YIELD path, weight" +
            " - using findAllPaths to get sources-sinks path or sinks-sources path with maxNodeLength")
    public Stream<PathResult> findAllPaths(@Name("startNodes") List<Node> startNodes,
                                       @Name("endNodes") List<Node> endNodes,
                                       @Name("maxNodeLength") long maxNodeLength,
                                       @Name("isBackward") boolean isBackward,
                                       @Name("isDepthFirst") boolean isDepthFirst){
        State initialState;

        if(isBackward){
            initialState = getInitialState(startNodes, null);
        }else{
            initialState = getInitialState(startNodes);
        }

        PathExpander<State> expander = new SimplePathExpander(
                ProcessorFactory.newInstance("Common"), false, isBackward);

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                expander, (int) maxNodeLength, initialState, isDepthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNodes, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.findJavaGadget(source, sinks, maxNodeLength, isBackward, depthFirst) YIELD path, weight" +
            " - using findJavaGadget to get source-sinks-gadget path")
    public Stream<PathResult> findJavaGadget(
            @Name("startNode") Node startNode,
            @Name("endNodes") List<Node> endNodes,
            @Name("maxNodeLength") long maxNodeLength,
            @Name("isBackward") boolean isBackward,
            @Name("depthFirst") boolean depthFirst) {

        String processor = isBackward ? "JavaGadgetBackward":"JavaGadget";

        PathExpander<State> expander = new SimplePathExpander(
                ProcessorFactory.newInstance(processor), false, isBackward);

        State initialState;

        if(isBackward){
            initialState = getInitialState(Collections.singletonList(startNode), null);
        }else{
            initialState = getInitialState(Collections.singletonList(startNode));
        }

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                expander, (int) maxNodeLength, initialState,
                depthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNode, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure
    @Description("tabby.algo.findAllJavaGadget(sources, sinks, maxNodeLength, isBackward, depthFirst) YIELD path, weight " +
            "- using findAllJavaGadget to get sources-sinks-gadget paths")
    public Stream<PathResult> findAllJavaGadget(
            @Name("startNodes") List<Node> startNodes,
            @Name("endNodes") List<Node> endNodes,
            @Name("maxNodeLength") long maxNodeLength,
            @Name("isBackward") boolean isBackward,
            @Name("depthFirst") boolean depthFirst) {

        String processor = isBackward ? "JavaGadgetBackward":"JavaGadget";

        PathExpander<State> expander = new SimplePathExpander(
                ProcessorFactory.newInstance(processor), false, isBackward);

        State initialState;

        if(isBackward){
            initialState = getInitialState(startNodes, null);
        }else{
            initialState = getInitialState(startNodes);
        }

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                expander, (int) maxNodeLength, initialState,
                depthFirst, new CommonJudgment()
        );

        Iterable<Path> allPaths = algo.findAllPaths(startNodes, endNodes);
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    /**
     * 为source节点生成初始状态
     * @param nodes
     * @return
     */
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

    /**
     * 为sink节点生成初始状态
     * @param nodes
     * @param polluted
     * @return
     */
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
