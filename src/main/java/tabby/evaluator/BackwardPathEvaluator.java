package tabby.evaluator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.PathEvaluator;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class BackwardPathEvaluator<STATE> extends PathEvaluator.Adapter<STATE> {

    private String endNodeSignature;
    private int maxDepth;

    public BackwardPathEvaluator(String endNodeSignature, int maxDepth) {
        this.endNodeSignature = endNodeSignature;
        this.maxDepth = maxDepth;
    }

    @Override
    public Evaluation evaluate(Path path, BranchState<STATE> state) {
        boolean includes = true;
        boolean continues = true;
        int length = path.length();
        Node node = path.endNode();

        if(length > maxDepth){
            includes = false;
        }

        if(length >= maxDepth){
            continues = false;
        }else if(node != null && endNodeSignature.equals(node.getProperty("SIGNATURE", "not exists"))){
            continues = false;
        }


        return Evaluation.of(includes, continues);
    }
}
