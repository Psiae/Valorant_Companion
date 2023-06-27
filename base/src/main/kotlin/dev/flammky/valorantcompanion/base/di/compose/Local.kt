package dev.flammky.valorantcompanion.base.di.compose

import androidx.compose.runtime.staticCompositionLocalOf
import dev.flammky.valorantcompanion.base.di.RuntimeDependencyInjector

val LocalRuntimeDependencyInjector = staticCompositionLocalOf<RuntimeDependencyInjector> {
    error("RuntimeDependencyInjector not provided")
}