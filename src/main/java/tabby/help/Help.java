package tabby.help;

import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import tabby.result.HelpResult;
import tabby.result.StringResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.neo4j.internal.helpers.collection.MapUtil.map;

/**
 * copy from apoc.help
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class Help {

    @Context
    public Transaction tx;

    private static final Set<String> extended = new HashSet<>();

    @Procedure("tabby.help")
    @Description("Provides descriptions of available procedures. To narrow the results, supply a search string. To also search in the description text, append + to the end of the search string.")
    public Stream<HelpResult> info(@Name("proc") String name) throws Exception {
        boolean searchText = false;
        if (name != null) {
            name = name.trim();
            if (name.endsWith("+")) {
                name = name.substring(0, name.lastIndexOf('+')).trim();
                searchText = true;
            }
        }
        String filter = " WHERE name starts with 'tabby.' " +
                " AND ($name IS NULL  OR toLower(name) CONTAINS toLower($name) " +
                " OR ($desc IS NOT NULL AND toLower(description) CONTAINS toLower($desc))) ";

        String proceduresQuery = "SHOW PROCEDURES yield name, description, signature " + filter +
                "RETURN 'procedure' as type, name, description, signature ";

        String functionsQuery = "SHOW FUNCTIONS yield name, description, signature " + filter +
                "RETURN 'function' as type, name, description, signature ";
        Map<String,Object> params = map( "name", name, "desc", searchText ? name : null );
        Stream<Map<String,Object>> proceduresResults = tx.execute( proceduresQuery, params ).stream();
        Stream<Map<String,Object>> functionsResults = tx.execute( functionsQuery, params ).stream();

        return Stream.of( proceduresResults, functionsResults ).flatMap( results -> results.map(
                row -> new HelpResult( row, !extended.contains( (String) row.get( "name" ) ) ) ) );
    }

    @Procedure("tabby.version")
    @Description("tabby path finder version")
    public Stream<StringResult> version() throws Exception {
        StringResult result = new StringResult("version 1.0, 20230130");
        return Stream.of(result);
    }
}
