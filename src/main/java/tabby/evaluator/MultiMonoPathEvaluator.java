package tabby.evaluator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.PathEvaluator;
import tabby.data.State;

import java.util.List;

/**
 * 一对多
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class MultiMonoPathEvaluator extends PathEvaluator.Adapter<State> {

    private List<Node> endNodes;
    private int maxDepth;

    public MultiMonoPathEvaluator(List<Node> endNodes, int maxDepth) {
        this.endNodes = endNodes;
        this.maxDepth = maxDepth;
    }

    @Override
    public Evaluation evaluate(Path path, BranchState<State> state) {
        boolean includes = true;
        boolean continues = true;
        int length = path.length();
        if(length == 0) return Evaluation.of(false, true);

        Node node = path.endNode();

        if(length >= maxDepth){
            continues = false; // 超出长度 不继续进行
            if(endNodes != null && !endNodes.contains(node)){
                includes = false; // 最后的节点不是endNode，不保存当前结果
            }
        }else if(endNodes != null && endNodes.contains(node)){
            // 长度没到，但已经找到了endNode，停止进行
            continues = false;
        } else {
            includes = false;
        }

        return Evaluation.of(includes, continues);
    }

    public static MultiMonoPathEvaluator of(List<Node> endNodes, int maxDepth){
        return new MultiMonoPathEvaluator(endNodes, maxDepth);
    }
}
