package tabby.evaluator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.PathEvaluator;
import tabby.data.State;

/**
 * 一对一
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class MonoPathEvaluator extends PathEvaluator.Adapter<State> {

    private Node endNode;
    private int maxDepth;

    public MonoPathEvaluator(Node endNode, int maxDepth) {
        this.endNode = endNode;
        this.maxDepth = maxDepth;
    }

    @Override
    public Evaluation evaluate(Path path, BranchState<State> state) {
        boolean includes = true;
        boolean continues = true;
        int length = path.length();
        Node node = path.endNode();

        if(length >= maxDepth){
            continues = false; // 超出长度 不继续进行
            if(endNode != null && !endNode.equals(node)){
                includes = false; // 最后的节点不是endNode，不保存当前结果
            }
        }else if(length == 0){ // 开始节点
            includes = false;
        } else if(endNode != null && endNode.equals(node)){
            // 长度没到，但已经找到了endNode，停止进行
            continues = false;
        } else {
            includes = false;
        }

        return Evaluation.of(includes, continues);
    }

    public static MonoPathEvaluator of(Node endNode, int maxDepth){
        return new MonoPathEvaluator(endNode, maxDepth);
    }
}
