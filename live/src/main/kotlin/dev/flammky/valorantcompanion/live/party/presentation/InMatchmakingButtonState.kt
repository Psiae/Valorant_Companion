package dev.flammky.valorantcompanion.live.party.presentation

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.AndroidUiDispatcher
import dev.flammky.valorantcompanion.time.truetime.LazyTimeKeeper
import dev.flammky.valorantcompanion.time.truetime.TrueTimeService
import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun rememberInMatchmakingButtonPresenter(
    trueTime: TrueTimeService = getFromKoin()
) = remember(trueTime) { InMatchmakingButtonPresenter(trueTime) }

class InMatchmakingButtonPresenter(
    private val trueTime: TrueTimeService
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
            elapsedTime = observeElapsedTimeInSeconds(timestamp = timeStamp)
        }
    }


    @Composable
    private fun observeElapsedTimeInSeconds(
        timestamp: Duration,
    ): Duration {
        Log.d("InMatchmakingButtonState", "ObserveElapsedTimeInSeconds($timestamp)")
        val syncState = remember(this) {
            mutableStateOf( Duration.ZERO)
        }
        val coroutineScope = rememberCoroutineScope()
        DisposableEffect(
            key1 = this,
            effect = {
                val keeper = trueTime.getLazyTimeKeeper(DurationUnit.HOURS)
                    ?: return@DisposableEffect onDispose {  }
                val supervisor = SupervisorJob()
                keeper.addLifetime(supervisor)
                var latestJob: Job? = null
                val observer = LazyTimeKeeper.SyncObserver { data ->
                    @OptIn(ExperimentalStdlibApi::class)
                    // CoroutineContext is not used so it doesn't matter here
                    AndroidUiDispatcher.Main[CoroutineDispatcher]!!.dispatch(EmptyCoroutineContext) {
                        latestJob?.cancel()
                        latestJob = coroutineScope.launch {
                            syncState.value = keeper.currentFromRaw(data)
                            while (isActive) {
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
        return (syncState.value - timestamp).coerceAtLeast(Duration.ZERO).also {
            Log.d("InMatchmakingButtonState", "ObserveElapsedTimeInSeconds($timestamp) {${syncState.value}}: $it")
        }
    }
}


class InMatchmakingButtonState(
    val isUserOwner: Boolean,
    val cancelMatchmaking: () -> Unit,
) {

    var elapsedTime by mutableStateOf<Duration>(Duration.ZERO)
}

private class Memento<T>(
    initialValue: T,
) {
    var current = initialValue
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