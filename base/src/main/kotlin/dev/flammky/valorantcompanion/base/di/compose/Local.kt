package dev.flammky.valorantcompanion.base.di.compose

import androidx.compose.runtime.staticCompositionLocalOf
import dev.flammky.valorantcompanion.base.di.DependencyInjector

val LocalDependencyInjector = staticCompositionLocalOf<DependencyInjector> {
    error("RuntimeDependencyInjector not provided")
}