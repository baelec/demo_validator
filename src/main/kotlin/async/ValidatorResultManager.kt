package async

import validator.Severity
import validator.ValidatorResult
import java.util.*
import java.util.Collections.synchronizedList
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

class ValidatorResultManager(
    executorService: ExecutorService,
    private val unexpectedExceptionHandler: (manager: ValidatorResultManager, throwable: Throwable) -> Unit
) {
    private val executorService = if (executorService !is BackPressureExecutorService) BackPressureExecutorService(executorService) else executorService
    private val tasks = synchronizedList<CompletableFuture<*>>(mutableListOf())
    private val results = synchronizedList<ValidatorResult>(mutableListOf())

    private var stopped = AtomicBoolean(false)
    val hasFailures: Boolean
        get() = results.any { it.severity.ordinal > Severity.TEMPORARY_ERROR.ordinal }

    fun stop() {
        stopped.set(true)
    }

    fun add(result: ValidatorResult) {
        results.add(result)
    }


    fun get(): CompletableFuture<List<ValidatorResult>> {
        return CompletableFuture.supplyAsync {
            while(!stopped.get()) {
                if (tasks.isEmpty()) {
                    stop()
                } else {
                    synchronized(tasks) {
                        if (tasks.all { it.isDone }) {
                            stop()
                        } else {
                            try {
                                CompletableFuture.allOf(*tasks.toTypedArray()).get()
                            } catch (e: Exception) {
                                stop()
                            }
                        }
                    }
                }
            }
            results
        }
    }

    fun submit(task: () -> Unit) {
        val task = CompletableFuture.supplyAsync(task, executorService)
        tasks.add(task)
    }
}