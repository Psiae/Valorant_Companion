package dev.flammky.valorantcompanion.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.base.kt.cast
import dev.flammky.valorantcompanion.base.util.mutableValueContainerOf

@Composable
inline fun <T, R> T.rememberThis(
    vararg keys: Any,
    crossinline init: @DisallowComposableCalls T.() -> R
) = remember(this, *keys) { init() }

@Composable
inline fun <T, R> T.runRemember(
    vararg keys: Any,
    crossinline init: @DisallowComposableCalls T.() -> R
) = remember(keys) { init() }

@Composable
inline fun <T, R> T.runRemember(
    crossinline init: @DisallowComposableCalls T.() -> R
) = remember() { init() }


@Composable
fun <K, V> rememberWithEquality(
    key: K,
    keyEquality: (old: K, new: K) -> Boolean,
    init: @DisallowComposableCalls () -> V
): V {
    return remember {
        object {
            var _key: Any? = RememberKtObj
            var _value: Any? = RememberKtObj
        }
    }.apply {
        if (_value == RememberKtObj || !keyEquality.invoke(_key as K, key)) {
            _key = key
            _value = init()
        }
    }._value as V
}

private object RememberKtObj

