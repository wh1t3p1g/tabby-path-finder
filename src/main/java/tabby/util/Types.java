package tabby.util;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.kernel.impl.core.RelationshipEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.neo4j.graphdb.Direction.*;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class Types {

    private final static int NO_ID = -1;
    private static int ALIAS_TYPE_ID = NO_ID;
    private static int CALL_TYPE_ID = NO_ID;

    public static Direction directionFor(String type) {
        if (type.contains("<")) return INCOMING;
        if (type.contains(">")) return OUTGOING;
        return BOTH;
    }

    public static RelationshipType relationshipTypeFor(String name) {
        name = name.replace("<", "").replace(">", "");
        return name.trim().isEmpty() ? null : RelationshipType.withName(name);
    }

    public static boolean isAlias(Relationship relationship){
        boolean isAlias = false;

        if(relationship != null){
            int typeId = NO_ID;

            if(relationship instanceof RelationshipEntity){
                // reflect type id
                try {
                    Method method = RelationshipEntity.class.getDeclaredMethod("typeId");
                    method.setAccessible(true);
                    typeId = (int) method.invoke(relationship);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    // ignore
                    e.printStackTrace();
                }
            }

            if(typeId == NO_ID || ALIAS_TYPE_ID == NO_ID || CALL_TYPE_ID == NO_ID){
                RelationshipType type = relationship.getType();
                isAlias = "ALIAS".equals(type.name());
                if(typeId != NO_ID){
                    if(isAlias){
                        ALIAS_TYPE_ID = typeId;
                    }else{
                        CALL_TYPE_ID = typeId;
                    }
                }
            }else{
                isAlias = typeId == ALIAS_TYPE_ID;
            }

        }
        return isAlias;
    }
}
