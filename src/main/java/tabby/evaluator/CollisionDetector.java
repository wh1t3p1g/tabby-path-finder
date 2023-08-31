package tabby.evaluator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.impl.traversal.StandardBranchCollisionDetector;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalBranch;
import tabby.data.Pollution;
import tabby.data.TabbyState;

import java.util.function.Predicate;

/**
 * @author wh1t3p1g
 * @since 2023/8/26
 */
public class CollisionDetector extends StandardBranchCollisionDetector {

    public CollisionDetector(Evaluator evaluator, Predicate<Path> pathPredicate) {
        super(evaluator, pathPredicate);
    }


    @Override
    protected boolean includePath(Path path, TraversalBranch startPath, TraversalBranch endPath) {
        Pollution startPol = getPollution(startPath);
        Pollution endPol = getPollution(endPath);
        return Pollution.compare(startPol, endPol);
    }

    public Pollution getPollution(TraversalBranch path){
        Relationship relationship = path.lastRelationship();
        TabbyState state = (TabbyState) path.state();
        String id;
        if(relationship == null){
            Node node = path.endNode();
            id = "node_"+node.getId();
        }else{
            id = String.valueOf(relationship.getId());
        }
        return state.get(id);
    }
}
