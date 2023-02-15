package tabby.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.neo4j.graphdb.Relationship;
import tabby.util.JsonHelper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author wh1t3p1g
 * @since 2023/1/28
 */
public class EdgeCache {

    public static EdgeCache rel = new EdgeCache();
    private LoadingCache<Relationship, int[][]> caching;

    public EdgeCache() {
        caching = CacheBuilder.newBuilder()
                .maximumSize(30000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<>(){
                    @Override
                    public int[][] load(Relationship edge) throws Exception {
                        try{
                            String pollutedStr = (String) edge.getProperty("POLLUTED_POSITION");
                            if(pollutedStr == null) return new int[0][];
                            return JsonHelper.parse(pollutedStr);
                        }catch (Exception e){
                            return new int[0][];
                        }
                    }
                });
    }

    public int[][] get(Relationship edge){
        try {
            return caching.get(edge);
        } catch (ExecutionException e) {
            return new int[0][];
        }
    }

    public void put(Relationship edge, int[][] values){
        caching.put(edge, values);
    }

    public long size(){
        return caching.size();
    }
}
