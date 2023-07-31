package dev.flammky.valorantcompanion.base.compose

import androidx.compose.runtime.*

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
inline fun <R> rememberWithCompositionObserver(
    key: Any?,
    noinline onRemembered: (R) -> Unit,
    noinline onForgotten: (R) -> Unit,
    noinline onAbandoned: (R) -> Unit,
    crossinline block: @DisallowComposableCalls () -> R
): R {
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
fun <T, R> rememberWithCustomEquality(
    key: Any?,
    equality: (old: Any?, new: Any?) -> Boolean,
    calculation: @androidx.compose.runtime.DisallowComposableCalls () -> R
): R {
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
    }.state.value as R
}

private object RememberKtObj