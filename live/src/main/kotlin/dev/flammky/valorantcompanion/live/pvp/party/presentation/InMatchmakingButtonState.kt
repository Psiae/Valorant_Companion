package dev.flammky.valorantcompanion.live.pvp.party.presentation

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.AndroidUiDispatcher
import dev.flammky.valorantcompanion.base.loop
import dev.flammky.valorantcompanion.base.compose.composeWithKey
import dev.flammky.valorantcompanion.base.time.truetime.LazyTimeKeeper
import dev.flammky.valorantcompanion.base.time.truetime.TimeKeeperService
import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.DurationUnit
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun rememberInMatchmakingButtonPresenter(
    trueTime: TimeKeeperService = getFromKoin()
) = remember(trueTime) { InMatchmakingButtonPresenter(trueTime) }

class InMatchmakingButtonPresenter(
    private val timeKeeperService: TimeKeeperService
) {

    @Composable
    fun present(
        isUserOwner: Boolean,
        timeStamp: Duration,
        cancelMatchmakingKey: Any,
        cancelMatchmaking: () -> Unit
    ): InMatchmakingButtonState {
        return remember(this, isUserOwner, cancelMatchmakingKey) {
            InMatchmakingButtonState(isUserOwner, cancelMatchmaking)
        }.apply {
            composeWithKey(key1 = timeStamp) { rTimeStamp ->
                val oElapsed = observeElapsedTimeInSeconds(timestamp = rTimeStamp)
                hasElapsedTime = oElapsed != null
                elapsedTime = oElapsed ?: Duration.ZERO
            }
        }
    }

    @Composable
    private fun observeElapsedTimeInSeconds(
        timestamp: Duration,
    ): Duration? {
        Log.d("InMatchmakingButtonState", "ObserveElapsedTimeInSeconds($timestamp)")
        val keeper = timeKeeperService.getLazyTimeKeeperOfAtMost(DurationUnit.HOURS)
        val syncState = remember(this, keeper) {
            mutableStateOf<Duration?>(keeper.getWithSystemClockOffsetIfPresent())
        }
        val coroutineScope = rememberCoroutineScope()
        DisposableEffect(
            key1 = this,
            key2 = keeper,
            effect = {
                val supervisor = SupervisorJob()
                keeper.addLifetime(supervisor)
                var latestJob: Job? = null
                val observer = LazyTimeKeeper.SyncObserver { data ->
                    syncState.value = keeper.currentFromRaw(data)
                    @OptIn(ExperimentalStdlibApi::class)
                    // CoroutineContext is not used so it doesn't matter here
                    AndroidUiDispatcher.Main[CoroutineDispatcher]!!.dispatch(EmptyCoroutineContext) {
                        latestJob?.cancel()
                        latestJob = coroutineScope.launch {
                            loop {
                                delay(1000)
                                syncState.value = keeper.currentFromRaw(data)
                            }
                        }
                    }
                }
                keeper.addObserver(observer)
                onDispose {
                    supervisor.cancel()
                    keeper.removeObserver(observer)
                }
            }
        )
        return syncState.value?.let { duration -> duration - timestamp }
    }
}


class InMatchmakingButtonState(
    val isUserOwner: Boolean,
    val cancelMatchmaking: () -> Unit,
) {
    var hasElapsedTime by mutableStateOf(false)
    var elapsedTime by mutableStateOf<Duration>(Duration.ZERO)
}

private class Memento<T>(
    initialValue: T,
) {
    var current: T = initialValue
        private set
    var before: T? = null
        private set

    fun next(value: T) {
        before = current
        current = value
    }

    fun remember(value: T) {
        if (current != value) next(value)
    }
}