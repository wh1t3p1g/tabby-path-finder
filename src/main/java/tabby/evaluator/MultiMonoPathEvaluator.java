package tabby.evaluator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.PathEvaluator;
import tabby.evaluator.judgment.CommonJudgment;
import tabby.evaluator.judgment.Judgment;
import tabby.util.State;

import java.util.List;

/**
 * 一对多
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class MultiMonoPathEvaluator extends PathEvaluator.Adapter<State> {

    private List<Node> endNodes;
    private int maxDepth;
    private Judgment judgment;

    public MultiMonoPathEvaluator(List<Node> endNodes, int maxDepth) {
        this(endNodes, maxDepth, new CommonJudgment());
    }

    public MultiMonoPathEvaluator(List<Node> endNodes, int maxDepth, Judgment judgment) {
        this.endNodes = endNodes;
        this.maxDepth = maxDepth;
        this.judgment = judgment;
    }

    @Override
    public Evaluation evaluate(Path path, BranchState<State> state) {
        return judgment.judge(path, state.getState(), endNodes, maxDepth);
    }

    public static MultiMonoPathEvaluator of(List<Node> endNodes, int maxDepth){
        return new MultiMonoPathEvaluator(endNodes, maxDepth);
    }

    public static MultiMonoPathEvaluator of(List<Node> endNodes, int maxDepth, Judgment judgment){
        return new MultiMonoPathEvaluator(endNodes, maxDepth, judgment);
    }
}
