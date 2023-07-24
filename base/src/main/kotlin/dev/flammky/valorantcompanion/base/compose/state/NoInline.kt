package dev.flammky.valorantcompanion.base.compose.state

import androidx.compose.runtime.Composable

@Composable
fun <R> subCompose(block: @Composable () -> R): R = block()