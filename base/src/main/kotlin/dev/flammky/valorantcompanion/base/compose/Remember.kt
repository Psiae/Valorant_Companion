package dev.flammky.valorantcompanion.base.compose

import androidx.compose.runtime.*
import androidx.compose.runtime.RememberObserver

@Composable
fun CompositionObserver(
    key: Any? = Unit,
    onRemembered: () -> Unit,
    onForgotten: () -> Unit,
    onAbandoned: () -> Unit,
) {
    val upOnRemembered = rememberUpdatedState(newValue = onRemembered)
    val upOnForgotten = rememberUpdatedState(newValue = onForgotten)
    val upOnAbandoned = rememberUpdatedState(newValue = onAbandoned)
    remember(key) {
        object : RememberObserver {

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
    }
}

@Composable
inline fun <T> rememberWithCompositionObserver(
    key: Any?,
    noinline onRemembered: (T) -> Unit,
    noinline onForgotten: (T) -> Unit,
    noinline onAbandoned: (T) -> Unit,
    crossinline block: @DisallowComposableCalls () -> T
): T {
    val upOnRemembered = rememberUpdatedState(newValue = onRemembered)
    val upOnForgotten = rememberUpdatedState(newValue = onForgotten)
    val upOnAbandoned = rememberUpdatedState(newValue = onAbandoned)
    return remember(key) {
        object : RememberObserver {
            val value = block()

            override fun onAbandoned() {
                upOnAbandoned.value.invoke(value)
            }

            override fun onForgotten() {
                upOnForgotten.value.invoke(value)
            }

            override fun onRemembered() {
                upOnRemembered.value.invoke(value)
            }
        }
    }.value
}

@Composable
fun <T> rememberWithCustomEquality(
    key: Any?,
    equality: (old: Any?, new: Any?) -> Boolean,
    calculation: @DisallowComposableCalls () -> T
): T {
    return remember {
        object {
            var latestKey: Any? = RememberKtObj
            val state = mutableStateOf<Any?>(RememberKtObj)
        }
    }.apply {
        if (latestKey == RememberKtObj || !equality.invoke(latestKey, key)) {
            latestKey = key
            state.value = calculation()
        }
    }.state.value as T
}

private object RememberKtObj