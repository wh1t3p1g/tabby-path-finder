package tabby.algo.release;

import org.neo4j.graphalgo.BasicEvaluationContext;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import tabby.data.State;
import tabby.expander.SimplePathExpander;
import tabby.expander.processor.ProcessorFactory;
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
public class JavaGadgetPathFinding {

    @Context
    public GraphDatabaseService db;

    @Context
    public Transaction tx;

    @Procedure("tabby.algo.findJavaGadget")
    @Description("tabby.algo.findJavaGadget(source, direct, sink, maxNodeLength, isDepthFirst) YIELD path, weight" +
            " - using findJavaGadget to get source-sink-gadget path")
    public Stream<PathResult> findJavaGadget(
            @Name("source") Node sourceNode,
            @Name("direct") String direct,
            @Name("sink") Node sinkNode,
            @Name("maxNodeLength") Long maxNodeLength,
            @Name("isDepthFirst") boolean isDepthFirst) {

        return findJavaGadgetWithState(sourceNode, direct, sinkNode, null, maxNodeLength, isDepthFirst);
    }

    @Procedure("tabby.algo.findJavaGadgetWithState")
    @Description("tabby.algo.findJavaGadgetWithState(source, direct, sink, sinkState, maxNodeLength, isDepthFirst) YIELD path, weight" +
            " - using findJavaGadget to get source-sink-gadget path with sink state")
    public Stream<PathResult> findJavaGadgetWithState(
            @Name("source") Node sourceNode,
            @Name("direct") String direct,
            @Name("sink") Node sinkNode,
            @Name("sinkState") String state,
            @Name("maxNodeLength") Long maxNodeLength,
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
                expander, maxNodeLength.intValue(), initialState, isDepthFirst
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
