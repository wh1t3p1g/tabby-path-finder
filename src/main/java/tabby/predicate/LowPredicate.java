package tabby.predicate;

import org.neo4j.graphdb.Relationship;

import java.util.Map;
import java.util.function.Predicate;

/**
 * 检查当前node与call边是否符合：
 * 1. propagated 为true
 * 2. REAL_CALL_TYPE符合当前调用者本身
 * @author wh1t3p1g
 * @since 2022/1/7
 */
public class LowPredicate implements Predicate<Relationship> {

    private String nodeType = "";

    public LowPredicate(String nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public boolean test(Relationship relationship) {
        Map<String, Object> properties = relationship.getProperties("PROPAGATED", "REAL_CALL_TYPE");
        boolean propagated = (boolean) properties.getOrDefault("PROPAGATED", true);

        if(propagated){
            String realCallType = (String) properties.getOrDefault("REAL_CALL_TYPE", "");
            if(!realCallType.isEmpty()){
                propagated = realCallType.equals(nodeType);
            }
        }

        return propagated;
    }
}
