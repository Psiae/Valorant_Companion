package dev.flammky.valorantcompanion.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
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
inline fun rememberKey(
    key: Any?,
    equality: (Any?, Any?) -> Boolean,
    crossinline newKey: @DisallowComposableCalls (Any?) -> Any? = { Any() }
): Any? {
    return remember {
        mutableValueContainerOf<Any?>(newKey(key))
    }.apply {
        if (!equality.invoke(key, value)) value = newKey(key)
    }.value
}

