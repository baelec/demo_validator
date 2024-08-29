package async;

import async.ShutdownOnFailure.SubTask.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ShutdownOnFailureTest {
    StructuredTaskScope.ShutdownOnFailure real;
    ShutdownOnFailure fake;
    ExecutorService executor;

    Callable<String> exceptionCallable = () -> { throw new RuntimeException("fake"); };
    Callable<String> stringCallable = () -> "test";

    @BeforeEach
    void beforeEach() {
        executor = Executors.newFixedThreadPool(5);
        real = new StructuredTaskScope.ShutdownOnFailure();
        fake = new ShutdownOnFailure(executor);
    }
    @AfterEach
    void afterEach() {
        try {
            executor.shutdownNow();
        } catch (Throwable ignored) {}
        try {
            real.close();
        } catch (Throwable ignored) {}
        try {
            fake.close();
        } catch (Throwable ignored) {}
    }

    @Test
    void closeThrows() {
        var type = IllegalStateException.class;
        assertThrows(type, () -> {
            real.fork(stringCallable);
            real.close();
        });
        assertThrows(type, () -> {
            fake.fork(stringCallable);
            fake.close();
        });
    }


    @Test
    void throwIfFailedThrows() {
        var type = ExecutionException.class;
        assertThrows(type, () -> {
            real.fork(exceptionCallable);
            real.join();
            real.throwIfFailed();
        });
        assertThrows(type, () -> {
            fake.fork(exceptionCallable);
            fake.join();
            fake.throwIfFailed();
        });
    }

    @Test
    void getThrows() {
        var type = IllegalStateException.class;
        assertThrows(type, () -> {
            var value = real.fork(exceptionCallable);
            real.join();
            value.get();
        });

        assertThrows(type, () -> {
            var value = fake.fork(exceptionCallable);
            fake.join();
            value.get();
        });
    }


    @Test
    void stateIncomplete() {
        Callable<String> callable = () -> {
            Thread.sleep(Duration.ofSeconds(1));
            return "test";
        };
        var realValue = real.fork(callable);
        real.shutdown();
        assertEquals(StructuredTaskScope.Subtask.State.UNAVAILABLE, realValue.state());

        var fakeValue = fake.fork(callable);
        fake.shutdown();
        assertEquals(State.UNAVAILABLE, fakeValue.state());
    }
}