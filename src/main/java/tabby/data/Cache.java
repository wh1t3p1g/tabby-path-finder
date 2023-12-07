package tabby.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.neo4j.graphdb.Relationship;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author wh1t3p1g
 * @since 2023/1/28
 */
public class Cache {

    public static Cache rel = new Cache();
    private LoadingCache<List<Object>, Pollution> caching;

    public Cache() {
        caching = CacheBuilder.newBuilder()
                .maximumSize(30000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<>(){
                    @Override
                    public Pollution load(List<Object> objs) throws Exception {
                        Relationship edge = (Relationship) objs.get(0);
                        boolean isCheckType = (boolean) objs.get(1);
                        return Pollution.of(edge, isCheckType);
                    }
                });
    }

    public Pollution get(Relationship edge, boolean isCheckType){
        try {
            List<Object> objs = new LinkedList<>();
            objs.add(edge);
            objs.add(isCheckType);
            return caching.get(objs); // 内存中可能会存两份同样的edge数据，有types和无types
        } catch (ExecutionException e) {
            return null;
        }
    }

    public long size(){
        return caching.size();
    }
}
