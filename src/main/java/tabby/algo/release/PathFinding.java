package tabby.algo.release;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import tabby.algo.BasePathFinding;
import tabby.result.PathResult;

import java.util.stream.Stream;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class PathFinding extends BasePathFinding {

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
                                       @Name("maxNodeLength") Long maxNodeLength,
                                       @Name("isDepthFirst") boolean isDepthFirst){
        return findPathWithState(sourceNode, direct, sinkNode, null, maxNodeLength.intValue(), isDepthFirst, false, false, db, tx);
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
                                                @Name("maxNodeLength") Long maxNodeLength,
                                                @Name("isDepthFirst") boolean isDepthFirst){
        return findPathWithState(sourceNode, direct, sinkNode, state, maxNodeLength.intValue(), isDepthFirst, false, false, db, tx);
    }
}
