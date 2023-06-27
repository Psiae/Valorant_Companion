package dev.flammky.valorantcompanion.base.di.compose

import androidx.compose.runtime.Composable
import dev.flammky.valorantcompanion.base.di.requireInject

@Composable
inline fun <reified T: Any> runtimeInject(): T {
    return LocalRuntimeDependencyInjector.current.requireInject<T>()
}