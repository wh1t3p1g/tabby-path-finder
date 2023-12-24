package tabby.evaluator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.PathEvaluator;
import tabby.data.Pollution;
import tabby.data.TabbyState;

/**
 * @author wh1t3p1g
 * @since 2023/8/24
 */
public class TabbyEvaluator extends PathEvaluator.Adapter<TabbyState>{

    private Node endNode;
    private Number maxDepth;
    private boolean checkAuth;
    private boolean isBackward;
    private Pollution endPol = null;

    public TabbyEvaluator(Node endNode, TabbyState endState, Number maxDepth, boolean checkAuth, boolean isBackward) {
        this.endNode = endNode;
        this.maxDepth = maxDepth;
        this.checkAuth = checkAuth;
        this.isBackward = isBackward;
        if(endState != null){
            this.endPol = endState.get("node_"+endNode.getId());
        }
    }

    public static TabbyEvaluator of(Node endNode, TabbyState endState, Number maxDepth, boolean checkAuth, boolean isBackward){
        return new TabbyEvaluator(endNode, endState, maxDepth, checkAuth, isBackward);
    }
    @Override
    public Evaluation evaluate(Path path, BranchState<TabbyState> branchState) {
        boolean includes = true;
        boolean continues = true;
        Long length = (long) path.length();
        Node node = path.endNode();

        if(length >= (Long)maxDepth){
            continues = false; // 超出长度 不继续进行
            if(endNode != null && !endNode.equals(node)){
                includes = false; // 最后的节点不是endNode，不保存当前结果
            }
        }else if(length == 0){ // 开始节点
            includes = false;
        } else if(endNode != null && endNode.equals(node)){
            // 长度没到，但已经找到了endNode，停止进行
            continues = false;
            if(checkAuth){
                TabbyState state = branchState.getState();
                Relationship edge = path.lastRelationship();
                if(edge != null && state != null){
                    String id = String.valueOf(edge.getId());
                    Pollution pollution = state.get(id);
                    includes = pollution.isContainsAuth();
                }
            }

            if(includes && !isBackward && endPol != null){
                TabbyState state = branchState.getState();
                Relationship edge = path.lastRelationship();
                if(edge != null && state != null){
                    String id = String.valueOf(edge.getId());
                    Pollution pollution = state.get(id);
                    includes = Pollution.compare(pollution, endPol);
                }
            }
        } else {
            includes = false;
        }

        return Evaluation.of(includes, continues);
    }
}
