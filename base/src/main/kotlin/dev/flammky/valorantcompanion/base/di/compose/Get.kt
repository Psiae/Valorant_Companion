package dev.flammky.valorantcompanion.base.di.compose

import androidx.compose.runtime.Composable
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.rememberThis

@Composable
inline fun <reified T: Any> inject(): T {
    return LocalDependencyInjector.current.rememberThis { requireInject() }
}