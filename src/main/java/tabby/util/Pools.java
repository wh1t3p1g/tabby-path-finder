package tabby.util;

import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * from apoc.Pools
 * @author wh1t3p1g
 * @since 2022/1/7
 */
public class Pools{
    public final static int DEFAULT_POOL_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    private final ExecutorService singleExecutorService;
    private final ExecutorService defaultExecutorService;

    public Pools(){
        int threads = Math.max(1, DEFAULT_POOL_THREADS);

        int queueSize = Math.max(1, threads * 5);

        // ensure we use daemon threads everywhere
        ThreadFactory threadFactory = r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        };
        this.singleExecutorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize),
                threadFactory, new CallerBlocksPolicy());

        this.defaultExecutorService = new ThreadPoolExecutor(threads / 2, threads, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize),
                threadFactory, new CallerBlocksPolicy());

    }

    public void shutdown() throws Exception {
        Stream.of(singleExecutorService, defaultExecutorService).forEach(service -> {
            try {
                service.shutdown();
                service.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignore) {
            }
        });
    }

    public ExecutorService getSingleExecutorService() {
        return singleExecutorService;
    }

    public ExecutorService getDefaultExecutorService() {
        return defaultExecutorService;
    }

    public static <T> T force(Future<T> future) throws ExecutionException {
        while (true) {
            try {
                return future.get();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    public static Pools newInstance(){
        return new Pools();
    }

    static class CallerBlocksPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // Submit again by directly injecting the task into the work queue, waiting if necessary, but also periodically checking if the pool has been
            // shut down.
            FutureTask<Void> task = new FutureTask<>( r, null );
            BlockingQueue<Runnable> queue = executor.getQueue();
            while (!executor.isShutdown()) {
                try {
                    if ( queue.offer( task, 250, TimeUnit.MILLISECONDS ) )
                    {
                        while ( !executor.isShutdown() )
                        {
                            try
                            {
                                task.get( 250, TimeUnit.MILLISECONDS );
                                return; // Success!
                            }
                            catch ( TimeoutException ignore )
                            {
                                // This is fine an expected. We just want to check that the executor hasn't been shut down.
                            }
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
