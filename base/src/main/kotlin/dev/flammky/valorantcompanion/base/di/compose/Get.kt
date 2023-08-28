package dev.flammky.valorantcompanion.base.di.compose

import androidx.compose.runtime.Composable
import dev.flammky.valorantcompanion.base.di.requireInject

// TODO: verify that we can just do `return LocalDependencyInjector.current`
@Composable
inline fun <reified T: Any> inject(): T = LocalDependencyInjector.current.requireInject()