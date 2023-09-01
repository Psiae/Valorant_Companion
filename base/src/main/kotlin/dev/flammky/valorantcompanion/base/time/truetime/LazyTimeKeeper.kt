package dev.flammky.valorantcompanion.base.time.truetime

import kotlinx.coroutines.Job
import kotlin.time.Duration

interface LazyTimeKeeper {

    fun addLifetime(job: Job)

    fun addObserver(observer: SyncObserver)

    fun removeObserver(observer: SyncObserver)

    fun getRawIfPresent(): Duration?
    fun getWithSystemClockOffsetIfPresent(): Duration?
    fun currentFromRaw(data: UpSyncData): Duration

    fun interface SyncObserver {
        /**
         * block to invoke on every time change by remote sync, including the current saved time,
         * the block is blocking the keeper, so this must be fast and non-blocking,
         * preferably just post it to your own Thread
         */
        fun upSync(time: UpSyncData)
    }

    data class UpSyncData(
        val time: Duration,
        internal val data: Any
    )
}