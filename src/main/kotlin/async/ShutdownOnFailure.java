package async;

import async.ShutdownOnFailure.SubTask.State;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;


public final class ShutdownOnFailure implements AutoCloseable {
    private final ExecutorService executor;
    private final ConcurrentLinkedQueue<SubTask<?>> threadFlock = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean joinedRef = new AtomicBoolean(true);
    private final AtomicBoolean shutdownRef = new AtomicBoolean(false);
    private final AtomicReference<Throwable> exceptionRef = new AtomicReference<>(null);
    private final ReentrantLock shutdownLock = new ReentrantLock();
    private final ReentrantLock joinLock = new ReentrantLock();

    public ShutdownOnFailure(final ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void close() {
        if (isShutdown()) {
            return;
        }
        if (!joinedRef.get()) {
            throw new IllegalStateException("Owner did not join after forking subtasks", null);
        }
        shutdown();
    }

    void throwIfFailed() throws ExecutionException {
        final var exception = exceptionRef.get();
        if (exception != null) {
            throw new ExecutionException(exception);
        }
    }

    void throwIfFailed(final Function<Throwable, Throwable> mapper) throws ExecutionException {
        final var exception = exceptionRef.get();
        if (exception != null) {
            throw new ExecutionException(mapper.apply(exception));
        }
    }

    void join() throws InterruptedException {
        try {
            joinLock.lock();
            joinedRef.set(true);
            CompletableFuture.allOf(threadFlock.stream().map(it -> it.future).toList().toArray(new CompletableFuture[]{})).join();
        } catch (Throwable ignored) {} finally {
            joinLock.unlock();
        }
    }

    void joinUntil(final Instant deadline) throws InterruptedException, TimeoutException {
        final var seconds = Duration.between(Instant.now(), deadline).getSeconds();
        try {
            joinLock.lock();
            joinedRef.set(true);
            CompletableFuture.allOf(threadFlock.stream().map(it -> it.future).toList().toArray(new CompletableFuture[]{})).get(seconds, TimeUnit.SECONDS);
        } catch (Throwable ignored) {} finally {
            joinLock.unlock();
        }
    }

    private static class SwallowedException extends RuntimeException {
        SwallowedException(Throwable t) {
            super(t);
        }
    }

    public boolean isShutdown() {
        return shutdownRef.get();
    }

    public void shutdown() {
        if (isShutdown()) {
            return;
        }
        shutdownRef.set(true);
        try {
            shutdownLock.lock();
            for (SubTask<?> it : threadFlock) {
                try {
                    it.future.cancel(true);
                } catch (Throwable ignored) {}
            }
        } finally {
            shutdownLock.unlock();
        }
    }

    <T> SubTask<T> fork(final Callable<T> runnable) {
        joinedRef.set(false);
        var task = new SubTask<T>();
        threadFlock.add(task);

        final var future = CompletableFuture.supplyAsync(() -> {
            try {
                return runnable.call();
            } catch (SwallowedException e) {
                throw e;
            } catch (Throwable e) {
                throw new SwallowedException(e);
            }
        }, executor).whenComplete((value, exception) -> {
            ShutdownOnFailure.this.threadFlock.remove(task);
            if (isShutdown()) {
                return;
            }
            if (exception != null)  {
                if (exception instanceof SwallowedException) {
                    exception = exception.getCause();
                    task.exception = exception;
                    if (exceptionRef.get() != null) {
                        exceptionRef.set(exception);
                    }
                }
                task.state = State.FAILED;
                task.exception = exception;
                exceptionRef.set(exception);
            } else {
                task.state = State.SUCCESS;
            }
        });

        task.setFuture(future);
        return task;
    }

    public final class SubTask<T> {
        private CompletableFuture<T> future;
        private State state = State.UNAVAILABLE;
        private Throwable exception;

        private SubTask() {}

        private void setFuture(CompletableFuture<T> future) {
            this.future = future;
        }

        public T get() {
            try {
                var value = future.get();
                if (!isShutdown()) {
                    this.state = State.SUCCESS;
                    return value;
                }
                return null;
            } catch (Throwable e) {
                state = State.FAILED;
                this.exception = e;
                throw new IllegalStateException("Subtask not completed or did not complete successfully", e);
            }
        }

        public Throwable exception() {
            return exception;
        }

        public State state() {
            return state;
        }

        public enum State {
            UNAVAILABLE,
            SUCCESS,
            FAILED,
        }
    }
}