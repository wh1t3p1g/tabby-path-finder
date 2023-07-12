package tabby.algo;

import org.neo4j.graphalgo.BasicEvaluationContext;
import org.neo4j.graphalgo.impl.path.TraversalPathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import tabby.data.State;
import tabby.expander.SimplePathExpander;
import tabby.expander.processor.ProcessorFactory;
import tabby.path.BidirectionalTraversalPathFinder;
import tabby.path.MonoDirectionalTraversalPathFinder;
import tabby.result.PathResult;
import tabby.util.JsonHelper;
import tabby.util.PositionHelper;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class PathFindingBeta {

    @Context
    public GraphDatabaseService db;

    @Context
    public Transaction tx;

    @Procedure("tabby.beta.findPath")
    @Description("tabby.beta.findPath(source, direct, sink, maxNodeLength, isDepthFirst) YIELD path, weight" +
            " - using findPath to get source-sink path with maxNodeLength")
    public Stream<PathResult> findPath(@Name("source") Node sourceNode,
                                       @Name("direct") String direct,
                                       @Name("sink") Node sinkNode,
                                       @Name("maxNodeLength") long maxNodeLength,
                                       @Name("isDepthFirst") boolean isDepthFirst){
        return findPathWithState(sourceNode, direct, sinkNode, null, maxNodeLength, isDepthFirst);
    }

    /**
     * 后向分析，需提供 sink 函数的 污点数据
     * 不提供前向分析，即source to sink
     * @param sourceNode
     * @param sinkNode
     * @param state
     * @param maxNodeLength
     * @param isDepthFirst
     * @return
     */
    @Procedure("tabby.beta.findPathWithState")
    @Description("tabby.beta.findPathWithState(source, direct, sink, sinkState, maxNodeLength, isDepthFirst) YIELD path, weight" +
            " - using findPath to get source-sink path with maxNodeLength and state")
    public Stream<PathResult> findPathWithState(@Name("source") Node sourceNode,
                                                @Name("direct") String direct,
                                                @Name("sink") Node sinkNode,
                                                @Name("state") String state,
                                                @Name("maxNodeLength") long maxNodeLength,
                                                @Name("isDepthFirst") boolean isDepthFirst){
        boolean isBackward = "<".equals(direct);

        PathExpander<State> expander = new SimplePathExpander(
                ProcessorFactory.newInstance("Common"), false, isBackward);

        State sourceInitialState;
        State sinkInitialState;
        TraversalPathFinder algo;
        Iterable<Path> allPaths;

        if(">".equals(direct)){
            // forwarded analysis
            sourceInitialState = getSourceInitialState(sourceNode);
            algo = new MonoDirectionalTraversalPathFinder(
                    new BasicEvaluationContext(tx, db),
                    expander, (int) maxNodeLength, sourceInitialState, isDepthFirst
            );
            allPaths = algo.findAllPaths(sourceNode, sinkNode);
        }else if("<".equals(direct)){
            // backward analysis
            sinkInitialState = getSinkInitialState(sinkNode, state);
            algo = new MonoDirectionalTraversalPathFinder(
                    new BasicEvaluationContext(tx, db),
                    expander, (int) maxNodeLength, sinkInitialState, isDepthFirst
            );
            allPaths = algo.findAllPaths(sinkNode, sourceNode);
        }else{
            // both
            sourceInitialState = getSourceInitialState(sourceNode);
            sinkInitialState = getSinkInitialState(sinkNode, state);
            algo = new BidirectionalTraversalPathFinder(
                    new BasicEvaluationContext(tx, db),
                    expander, (int) maxNodeLength, sourceInitialState, sinkInitialState, isDepthFirst
            );
            allPaths = algo.findAllPaths(sourceNode, sinkNode);
        }

        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    @Procedure("tabby.beta.findJavaGadget")
    @Description("tabby.beta.findJavaGadget(source, direct, sink, maxNodeLength, isDepthFirst) YIELD path, weight" +
            " - using findJavaGadget to get source-sink-gadget path")
    public Stream<PathResult> findJavaGadget(
            @Name("source") Node sourceNode,
            @Name("direct") String direct,
            @Name("sink") Node sinkNode,
            @Name("maxNodeLength") long maxNodeLength,
            @Name("isDepthFirst") boolean isDepthFirst) {

        return findJavaGadgetWithState(sourceNode, direct, sinkNode, null, maxNodeLength, isDepthFirst);
    }

    @Procedure("tabby.beta.findJavaGadgetWithState")
    @Description("tabby.beta.findJavaGadgetWithState(source, direct, sink, sinkState, maxNodeLength, isDepthFirst) YIELD path, weight" +
            " - using findJavaGadget to get source-sink-gadget path with sink state")
    public Stream<PathResult> findJavaGadgetWithState(
            @Name("source") Node sourceNode,
            @Name("direct") String direct,
            @Name("sink") Node sinkNode,
            @Name("sinkState") String state,
            @Name("maxNodeLength") long maxNodeLength,
            @Name("isDepthFirst") boolean isDepthFirst) {
        boolean isBackward = "<".equals(direct);
        String processor = isBackward ? "JavaGadgetBackward":"JavaGadget";

        PathExpander<State> expander = new SimplePathExpander(
                ProcessorFactory.newInstance(processor), false, isBackward);

        State initialState;

        if(isBackward){
            initialState = getSinkInitialState(sinkNode, state);
        }else{
            initialState = getSourceInitialState(sourceNode);
        }

        MonoDirectionalTraversalPathFinder algo = new MonoDirectionalTraversalPathFinder(
                new BasicEvaluationContext(tx, db),
                expander, (int) maxNodeLength, initialState, isDepthFirst
        );

        Iterable<Path> allPaths;
        if(isBackward){
            allPaths = algo.findAllPaths(sinkNode, sourceNode);
        }else{
            allPaths = algo.findAllPaths(sourceNode, sinkNode);
        }
        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

    /**
     * 为source节点生成初始状态
     * @param node
     * @return
     */
    public State getSourceInitialState(Node node){
        State state = State.newInstance();
        long parameterSize = 0;
        try{
            parameterSize = (long) node.getProperty("PARAMETER_SIZE", 0);
        }catch (Exception ignore){}

        int initSize = (int)parameterSize + 1;
        int[][] initialPositions = new int[initSize][];
        initialPositions[0] = new int[]{PositionHelper.THIS}; // check this
        for(int i=0; i<parameterSize;i++){
            initialPositions[i+1] = new int[]{i};
        }
        state.addInitialPositions(node.getId(), initialPositions);

        return state;
    }

    /**
     * 为sink节点生成初始状态
     * @param node
     * @param polluted
     * @return
     */
    public State getSinkInitialState(Node node, String polluted){
        State state = State.newInstance();
        int[][] initialPositions = null;

        if(polluted != null){
            initialPositions = JsonHelper.parse(polluted);
        }

        if(initialPositions == null){
            Map<String, Object> properties = node.getProperties("IS_SINK", "POLLUTED_POSITION");
            boolean isSink = (boolean) properties.getOrDefault("IS_SINK", false);
            if(isSink){
                polluted = (String) properties.getOrDefault("POLLUTED_POSITION", "[]");
                state.addInitialPositions(node.getId(), JsonHelper.parse(polluted));
            }
        }else{
            state.addInitialPositions(node.getId(), initialPositions);
        }

        return state;
    }

}
