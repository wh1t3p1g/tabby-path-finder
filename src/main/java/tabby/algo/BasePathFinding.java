package tabby.algo;

import org.neo4j.graphalgo.BasicEvaluationContext;
import org.neo4j.graphalgo.impl.path.TraversalPathFinder;
import org.neo4j.graphdb.*;
import tabby.data.TabbyState;
import tabby.expander.TabbyPathExpander;
import tabby.path.TabbyBidirectionalTraversalPathFinder;
import tabby.path.TabbyTraversalPathFinder;
import tabby.result.PathResult;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author wh1t3p1g
 * @project tabby-path-finder
 * @since 2023/12/7
 */
public class BasePathFinding {

    public static Stream<PathResult> findPathWithState(Node sourceNode, String direct, Node sinkNode, String state,
                                                       int maxNodeLength, boolean isDepthFirst, boolean checkAuth, boolean isCheckType,
                                                       GraphDatabaseService db, Transaction tx){

        PathExpander<TabbyState> expander;
        TraversalPathFinder algo;
        Iterable<Path> allPaths;

        if(">".equals(direct)){
            expander = new TabbyPathExpander(false, false, isCheckType);
            TabbyState initialState = TabbyState.initialState(sourceNode);
            TabbyState sinkState = TabbyState.initialState(sinkNode, state);
            algo = new TabbyTraversalPathFinder(
                    new BasicEvaluationContext(tx, db),
                    expander, initialState, sinkState,
                    maxNodeLength, isDepthFirst, checkAuth, false);

            allPaths = algo.findAllPaths(sourceNode, sinkNode);
        }else if("<".equals(direct)){
            expander = new TabbyPathExpander(false, true, isCheckType);
            TabbyState initialState = TabbyState.initialState(sinkNode, state);
            if(initialState == null){
                maxNodeLength = 0;
            }
            algo = new TabbyTraversalPathFinder(
                    new BasicEvaluationContext(tx, db),
                    expander, initialState, null,
                    maxNodeLength, isDepthFirst, false, true);

            allPaths = algo.findAllPaths(sinkNode, sourceNode);
        }else{
            expander = new TabbyPathExpander(false, false, isCheckType);

            TabbyState sourceState = TabbyState.initialState(sourceNode);
            TabbyState sinkState = TabbyState.initialState(sinkNode, state);

            algo = new TabbyBidirectionalTraversalPathFinder(new BasicEvaluationContext(tx, db),
                    expander, sourceState, sinkState, maxNodeLength, isDepthFirst);

            allPaths = algo.findAllPaths(sourceNode, sinkNode);
        }

        return StreamSupport.stream(allPaths.spliterator(), true)
                .map(PathResult::new);
    }

}
