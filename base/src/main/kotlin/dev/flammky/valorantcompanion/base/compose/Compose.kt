package dev.flammky.valorantcompanion.base.compose

import androidx.compose.runtime.Composable

@Composable
fun <R> compose(block: @Composable () -> R): R = block()

@Composable
inline fun <R> inlineCompose(block: @Composable () -> R): R = block()