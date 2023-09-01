package dev.flammky.valorantcompanion.base.time.truetime.impl

import android.os.SystemClock
import android.util.Log
import androidx.annotation.GuardedBy
import com.instacart.truetime.sntp.SntpImpl
import com.instacart.truetime.time.TrueTimeImpl
import com.instacart.truetime.time.TrueTimeParameters
import dev.flammky.valorantcompanion.base.time.truetime.LazyTimeKeeper
import kotlinx.coroutines.*
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

internal class HourlyLazyTimeKeeper : LazyTimeKeeper {

    private val stateLock = Any()
    private val lifeSupports = mutableListOf<Job>()
    private var isAlive = false
    private val tru = TrueTimeOp()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null
    private var stamp = -1L

    override fun addLifetime(job: Job) {
        synchronized(lifeSupports) {
            lifeSupports.add(job)
            job.invokeOnCompletion {
                synchronized(lifeSupports) {
                    lifeSupports.remove(job)
                    if (lifeSupports.size == 0) sleep()
                }
            }
            if (lifeSupports.size == 1) wakeup()
        }
    }

    override fun addObserver(observer: LazyTimeKeeper.SyncObserver) {
        tru.addObserver(observer)
    }

    override fun removeObserver(observer: LazyTimeKeeper.SyncObserver) {
        tru.removeObserver(observer)
    }

    override fun getRawIfPresent(): Duration? {
        return tru.current()
    }

    override fun getWithSystemClockOffsetIfPresent(): Duration? {
        return tru.currentWithSystemClockOffset()
    }

    override fun currentFromRaw(data: LazyTimeKeeper.UpSyncData): Duration {
        return tru.currentWithSystemClockOffset(data) ?: throw IllegalArgumentException("data is invalid")
    }

    private fun wakeup() {
        Log.i("HourlyLazyTimeKeeper.kt", "$this waking up...")
        synchronized(stateLock) {
            if (isAlive) return
            isAlive = true
            job = coroutineScope.launch {
                if (stamp != -1L) {
                    delay(1.hours.inWholeMilliseconds - (SystemClock.elapsedRealtime() - stamp))
                    stamp = SystemClock.elapsedRealtime()
                }
                tru.strictSync()
            }
        }
    }

    private fun sleep() {
        Log.i("HourlyLazyTimeKeeper.kt", "$this sleeping...")
        synchronized(stateLock) {
            if (!isAlive) return
            isAlive = false
            job?.cancel()
            tru.strictCancel()
        }
    }

    private inner class TrueTimeOp {

        private val upSyncObservers = mutableListOf<LazyTimeKeeper.SyncObserver>()
        private var syncJob: Job? = null

        @Volatile
        private var keepNtpResult: NtpResult? = null

        private val tru = TrueTimeImpl(
            params = TrueTimeParameters.Builder()
                .syncIntervalInMillis(1.hours.inWholeMilliseconds)
                .buildParams(),
            listener = object : NoOpTrueTimeEventListenerInterface {
                override fun storingTrueTime(ntpResult: LongArray) {
                    val wrap = NtpResult(ntpResult)
                    keepNtpResult = wrap
                    val trueTime = Date(wrap.trueTime())
                    val duration = trueTime.time.milliseconds
                    val data = LazyTimeKeeper.UpSyncData(duration, wrap)
                    synchronized(upSyncObservers) {
                        upSyncObservers.forEach { it.upSync(data) }
                    }
                }
            }
        )

        @GuardedBy("stateLock")
        fun sync() {
            if (syncJob == null) {
                syncJob = tru.sync()
            }
        }

        @GuardedBy("stateLock")
        fun strictSync() {
            check(syncJob == null)
            syncJob = tru.sync()
        }

        @GuardedBy("stateLock")
        fun cancel() {
            syncJob?.cancel()
        }

        @GuardedBy("stateLock")
        fun strictCancel() {
            syncJob!!.cancel()
            syncJob = null
        }

        fun addObserver(observer: LazyTimeKeeper.SyncObserver) {
            synchronized(upSyncObservers) {
                upSyncObservers.add(observer)
                keepNtpResult?.let {
                    observer.upSync(LazyTimeKeeper.UpSyncData(it.trueTime().milliseconds, it))
                }
            }
        }

        fun removeObserver(observer: LazyTimeKeeper.SyncObserver) {
            synchronized(upSyncObservers) {
                upSyncObservers.remove(observer)
            }
        }

        fun current(): Duration? = keepNtpResult?.trueTime()?.milliseconds

        fun currentWithSystemClockOffset(): Duration? {
            return keepNtpResult?.let { currentWithSystemClockOffset(it) }
        }
        fun currentWithSystemClockOffset(data: LazyTimeKeeper.UpSyncData): Duration? {
            return (data.data as? NtpResult)?.let { currentWithSystemClockOffset(it) }
        }
        fun currentWithSystemClockOffset(ntpResult: NtpResult): Duration {
            val offset = SystemClock.elapsedRealtime() - ntpResult.elapsed()
            return (ntpResult.trueTime() + offset).milliseconds
        }
    }

    private class NtpResult(
        val arr: LongArray
    ) {
        val impl = SntpImpl()
        fun trueTime() = impl.trueTime(arr)
        fun elapsed() = impl.timeSinceBoot(arr)
    }
}