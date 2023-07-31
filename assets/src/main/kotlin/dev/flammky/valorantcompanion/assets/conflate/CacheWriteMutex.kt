package dev.flammky.valorantcompanion.assets.conflate

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

interface CacheWriteMutex {
    suspend fun write(block: suspend (handle: Job) -> Unit)
    suspend fun awaitUnlock()
}

internal class ConflatedCacheWriteMutex() : CacheWriteMutex {
    private val _mutex = Mutex()
    private val _lock = Any()
    /* private var _stagedWrite: CooperativeJob? = null */
    private var _currentWrite: CooperativeJob? = null

    override suspend fun write(block: suspend (handle: Job) -> Unit) {
        val handle = CooperativeJob()
        val current = synchronized(_lock) {
            _currentWrite?.apply { cancel() }.also {
                _currentWrite = handle
            }
        }
        val doBlock = runCatching {
            withContext(handle.coroutineJob) {
                current?.awaitCompletion()
                block(handle.coroutineJob)
            }
        }
        handle.complete()
        synchronized(_lock) {
            if (_currentWrite == handle) _mutex.unlock()
        }
        doBlock.onFailure { throw it }
    }

    override suspend fun awaitUnlock() = synchronized(_lock) { _mutex }.withLock {  }

    private class CooperativeJob() {
        private val _coroutineJob = Job()
        private val _completionJob = Job()

        val coroutineJob: Job
            get() = _coroutineJob

        fun cancel() {
            _coroutineJob.cancel()
        }
        
        fun complete() {
            _completionJob.complete()
        }

        suspend fun awaitCompletion() = _completionJob.join()
    }
}