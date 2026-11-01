package hotel.concurrent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
public class ProducerRunner {
    private final int threads;
    public ProducerRunner(int threads) {
        if (threads < 1) { throw new IllegalArgumentException("Threads must be positive"); }
        this.threads = threads;
    }
    public void run(List<Runnable> producers) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (Runnable producer : producers) { futures.add(executor.submit(producer)); }
        } finally {
            executor.shutdown();
            // Drain accepted tasks even when another producer fails. Publish is synchronous.
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    throw new IllegalStateException("Producers did not finish in 30 seconds");
                }
            } catch (InterruptedException error) {
                executor.shutdownNow();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    error.addSuppressed(new IllegalStateException("Producer ignored interruption"));
                }
                Thread.currentThread().interrupt();
                throw error;
            }
        }
        for (Future<?> future : futures) { future.get(); }
    }
}
