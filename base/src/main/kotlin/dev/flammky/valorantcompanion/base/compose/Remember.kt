package dev.flammky.valorantcompanion.base.compose

import android.util.Log
import androidx.compose.runtime.*

@Composable
inline fun <R> rememberNoKey(
    crossinline block: @DisallowComposableCalls () -> R
) = remember(Unit, block)

@Composable
inline fun <R> compositionRemember(
    crossinline block: @DisallowComposableCalls () -> R
) = remember(Any(), block)

@Composable
inline fun <R> rememberEffect(
    key: Any?,
    noinline onRemembered: () -> Unit,
    noinline onForgotten: () -> Unit,
    noinline onAbandoned: () -> Unit,
    crossinline block: @DisallowComposableCalls () -> R
): R {
    val upOnRemembered = rememberUpdatedState(newValue = onRemembered)
    val upOnForgotten = rememberUpdatedState(newValue = onForgotten)
    val upOnAbandoned = rememberUpdatedState(newValue = onAbandoned)
    return remember(key) {
        object : RememberObserver {
            val value = block()

            override fun onAbandoned() {
                upOnAbandoned.value.invoke()
            }

            override fun onForgotten() {
                upOnForgotten.value.invoke()
            }

            override fun onRemembered() {
                upOnRemembered.value.invoke()
            }
        }
    }.value
}