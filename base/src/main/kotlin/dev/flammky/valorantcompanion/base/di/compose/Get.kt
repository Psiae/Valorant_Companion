package dev.flammky.valorantcompanion.base.di.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.runRemember

@Composable
inline fun <reified T: Any> inject(): T {
    return LocalDependencyInjector.current.runRemember { requireInject() }
}