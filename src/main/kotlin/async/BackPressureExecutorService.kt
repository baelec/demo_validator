package async

import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException

class BackPressureExecutorService(private val executor: ExecutorService) : ExecutorService by executor {
    override fun <T : Any?> submit(task: Callable<T>): Future<T> {
        try {
            return executor.submit(task)
        } catch (_: RejectedExecutionException) {}
        // We should only get here if execution was rejected. That's okay, we'll run it on the main thread.
        return try {
            CompletableFuture.completedFuture(task.call())
        } catch (throwable: Throwable) {
            CompletableFuture.failedFuture(throwable)
        }
    }

    override fun execute(task: Runnable) {
        try {
            executor.execute(task)
            return
        } catch (_: RejectedExecutionException) {}
        // We should only get here if execution was rejected. That's okay, we'll run it on the main thread to provide backpressure
        task.run()
    }

    override fun submit(task: Runnable): Future<*> {
        try {
            return executor.submit(task)
        } catch (_: RejectedExecutionException) {}
        // We should only get here if execution was rejected. That's okay, we'll run it on the main thread to provide backpressure
        return try {
            CompletableFuture.completedFuture(task.run())
        } catch (throwable: Throwable) {
            CompletableFuture.failedFuture<Any?>(throwable)
        }
    }
}
