package dev.flammky.valorantcompanion.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember

@Composable
inline fun <T, R> T.runRemember(
    vararg keys: Any,
    crossinline init: @DisallowComposableCalls T.() -> R
) = remember(this, *keys) { init() }