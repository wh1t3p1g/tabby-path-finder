package tabby.algo;

import org.neo4j.graphalgo.BasicEvaluationContext;
import org.neo4j.graphalgo.impl.path.TraversalPathFinder;
import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import tabby.data.TabbyState;
import tabby.expander.TabbyPathExpander;
import tabby.path.TabbyBidirectionalTraversalPathFinder;
import tabby.path.TabbyTraversalPathFinder;
import tabby.result.PathResult;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class NewPathFinding {

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
        return findPathWithState(sourceNode, direct, sinkNode, null, maxNodeLength, isDepthFirst, false);
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
        return findPathWithState(sourceNode, direct, sinkNode, state, maxNodeLength, isDepthFirst, false);
    }

    @Procedure("tabby.beta.findPathWithAuth")
    @Description("tabby.beta.findPath(source, sink, maxNodeLength, isDepthFirst) YIELD path, weight" +
            " - using findPath to get source-sink path with maxNodeLength")
    public Stream<PathResult> findPathWithAuth(@Name("source") Node sourceNode,
                                                @Name("sink") Node sinkNode,
                                                @Name("maxNodeLength") long maxNodeLength,
                                                @Name("isDepthFirst") boolean isDepthFirst){

        return findPathWithState(sourceNode, ">", sinkNode, null, maxNodeLength, isDepthFirst, true);
    }



    public Stream<PathResult> findPathWithState(Node sourceNode, String direct, Node sinkNode, String state,
                                               long maxNodeLength, boolean isDepthFirst, boolean checkAuth){

        PathExpander<TabbyState> expander;
        TraversalPathFinder algo;
        Iterable<Path> allPaths;
        int maxDepth = (int) maxNodeLength;

        if(">".equals(direct)){
            expander = new TabbyPathExpander(false, false);
            TabbyState initialState = TabbyState.initialState(sourceNode);
            TabbyState sinkState = TabbyState.initialState(sinkNode, state);
            algo = new TabbyTraversalPathFinder(
                    new BasicEvaluationContext(tx, db),
                    expander, initialState, sinkState,
                    maxDepth, isDepthFirst, checkAuth, false);

            allPaths = algo.findAllPaths(sourceNode, sinkNode);
        }else if("<".equals(direct)){
            expander = new TabbyPathExpander(false, true);
            TabbyState initialState = TabbyState.initialState(sinkNode, state);
            if(initialState == null){
                maxNodeLength = 0;
            }
            algo = new TabbyTraversalPathFinder(
                    new BasicEvaluationContext(tx, db),
                    expander, initialState, null,
                    (int) maxNodeLength, isDepthFirst, false, true);

            allPaths = algo.findAllPaths(sinkNode, sourceNode);
        }else{
            expander = new TabbyPathExpander(false, false);

            TabbyState sourceState = TabbyState.initialState(sourceNode);
            TabbyState sinkState = TabbyState.initialState(sinkNode, state);

            algo = new TabbyBidirectionalTraversalPathFinder(new BasicEvaluationContext(tx, db),
                    expander, sourceState, sinkState, (int) maxNodeLength, isDepthFirst);

            allPaths = algo.findAllPaths(sourceNode, sinkNode);
        }

        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

}
