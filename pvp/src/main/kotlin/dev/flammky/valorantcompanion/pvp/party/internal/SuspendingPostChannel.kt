package dev.flammky.valorantcompanion.pvp.party.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PostChannelResult(
    private val job: Job
) {

    fun invokeOnCompletion(completionHandler: CompletionHandler) {
        job.invokeOnCompletion(completionHandler)
    }
}

internal interface SuspendingPostChannel {

    // TODO: return immediate post result
    fun <R> post(block: suspend () -> R): PostChannelResult
}

internal class SuspendingPostChannelImpl(
    private val dispatcher: CoroutineDispatcher
): SuspendingPostChannel {

    private class Element(
        val job: CompletableJob,
        val block: suspend () -> Any?
    )

    private val coroutineScope = CoroutineScope(dispatcher + SupervisorJob())
    private val array = mutableListOf<Element>()
    private val mutex = Mutex()
    private val lock = ReentrantLock()

    override fun <R> post(block: suspend () -> R): PostChannelResult {
        return lock.withLock {
            val element = Element(
                Job(),
                block
            )
            array.add(element)
            onNextAppended()
            PostChannelResult(
                element.job
            )
        }
    }

    private fun onNextAppended() {
        coroutineScope.launch {
            mutex.withLock {
                synchronized(lock) { array.removeFirst() }.apply {
                    runCatching { block() }
                        .onSuccess { job.complete() }
                        .onFailure { ex ->
                            job.completeExceptionally(ex)
                            throw ex
                        }
                }
            }
        }
    }
}