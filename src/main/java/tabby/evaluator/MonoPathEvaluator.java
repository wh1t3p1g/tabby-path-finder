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
    private Number maxDepth;
    private boolean checkAuth;

    public MonoPathEvaluator(Node endNode, Number maxDepth, boolean checkAuth) {
        this.endNode = endNode;
        this.maxDepth = maxDepth;
        this.checkAuth = checkAuth;
    }

    @Override
    public Evaluation evaluate(Path path, BranchState<State> state) {
        boolean includes = true;
        boolean continues = true;
        long length = path.length();
        Node node = path.endNode();

        if((Long)length >= (Long)maxDepth){
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

    public static MonoPathEvaluator of(Node endNode, Number maxDepth){
        return new MonoPathEvaluator(endNode, maxDepth, false);
    }

    public static MonoPathEvaluator of(Node endNode, int maxDepth, boolean checkAuth){
        return new MonoPathEvaluator(endNode, maxDepth, checkAuth);
    }
}
