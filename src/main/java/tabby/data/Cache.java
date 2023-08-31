package tabby.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.neo4j.graphdb.Relationship;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author wh1t3p1g
 * @since 2023/1/28
 */
public class Cache {

    public static Cache rel = new Cache();
    private LoadingCache<Relationship, Pollution> caching;

    public Cache() {
        caching = CacheBuilder.newBuilder()
                .maximumSize(30000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<>(){
                    @Override
                    public Pollution load(Relationship edge) throws Exception {
                        return Pollution.of(edge);
                    }
                });
    }

    public Pollution get(Relationship edge){
        try {
            return caching.get(edge);
        } catch (ExecutionException e) {
            return null;
        }
    }

    public long size(){
        return caching.size();
    }
}
