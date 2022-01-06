package tabby.util;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.internal.helpers.collection.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.graphdb.Direction.*;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class Types {
    public static final char BACKTICK = '`';

    public static RelationshipType getCallRelationshipType(){
        return RelationshipType.withName("CALL");
    }

    public static RelationshipType getAliasRelationshipType(){
        return RelationshipType.withName("ALIAS");
    }

    public static List<Pair<RelationshipType, Direction>> parse(String pathFilter) {
        List<Pair<RelationshipType, Direction>> relsAndDirs = new ArrayList<>();
        if (pathFilter == null) {
            relsAndDirs.add(Pair.of(null, BOTH));
        } else {
            String[] defs = pathFilter.split("\\|");
            for (String def : defs) {
                relsAndDirs.add(Pair.of(relationshipTypeFor(def), directionFor(def)));
            }
        }
        return relsAndDirs;
    }

    public static Direction directionFor(String type) {
        if (type.contains("<")) return INCOMING;
        if (type.contains(">")) return OUTGOING;
        return BOTH;
    }

    public static RelationshipType relationshipTypeFor(String name) {
        if (name.indexOf(BACKTICK) > -1) name = name.substring(name.indexOf(BACKTICK)+1,name.lastIndexOf(BACKTICK));
        else {
            name = name.replaceAll("[<>:]", "");
        }
        return name.trim().isEmpty() ? null : RelationshipType.withName(name);
    }
}
