package tabby.evaluator.judgment;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import tabby.data.State;

import java.util.List;

/**
 * @author wh1t3p1g
 * @since 2022/4/28
 */
public interface Judgment {

    Evaluation judge(Path path, State state, List<Node> endNodes, int maxDepth);
}
