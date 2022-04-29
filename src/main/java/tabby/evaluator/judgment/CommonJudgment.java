package tabby.evaluator.judgment;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import tabby.util.State;

import java.util.List;

/**
 * @author wh1t3p1g
 * @since 2022/4/28
 */
public class CommonJudgment implements Judgment{

    @Override
    public Evaluation judge(Path path, State state, List<Node> endNodes, int maxDepth) {
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
}
