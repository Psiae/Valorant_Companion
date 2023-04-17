package dev.flammky.valorantcompanion.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember

@Composable
fun <T, R> T.runRemember(
    vararg keys: Any,
    init: @DisallowComposableCalls T.() -> R
) = remember(this, *keys) { init() }