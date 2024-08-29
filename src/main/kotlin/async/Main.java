package async;

import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.*;

public class Main {
    private Random rand = new Random();
    public static int getDefaultSchedulersReactor() {
        return 10 * Runtime.getRuntime().availableProcessors();
    }

    public Runnable smallWorkflow() {
        return () -> {
            try {
                Thread.sleep(Duration.ofSeconds(rand.nextLong(5, 15)));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static ExecutorService executor() {
        return new ThreadPoolExecutor(
                Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
                Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()
        );
    }

    public static void main() {
        try (var executor = executor()) {
            try (var scope = new ShutdownOnFailure(executor)) {
                scope.fork(() -> {
                    Thread.sleep(Duration.ofSeconds(5));
                    throw new RuntimeException("cats");
                });
                scope.join();
                //scope.throwIfFailed();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
