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
public class PathFinding {

    @Context
    public GraphDatabaseService db;

    @Context
    public Transaction tx;

    @Procedure("tabby.algo.findPath")
    @Description("tabby.algo.findPath(source, direct, sink, maxNodeLength, isDepthFirst) YIELD path, weight" +
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
    @Procedure("tabby.algo.findPathWithState")
    @Description("tabby.algo.findPathWithState(source, direct, sink, sinkState, maxNodeLength, isDepthFirst) YIELD path, weight" +
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
