package dev.flammky.valorantcompanion.base.compose

import androidx.compose.runtime.Composable

@Composable
fun <R> compose(block: @Composable () -> R): R = block()

@Composable
fun <T1, R> compose(p1: T1, block: @Composable (T1) -> R): R = block(p1)

@Composable
fun <T1, T2, R> compose(p1: T1, p2: T2, block: @Composable (T1, T2) -> R): R = block(p1, p2)

@Composable
fun <T1, T2, T3, R> compose(p1: T1, p2: T2, p3: T3, block: @Composable (T1, T2, T3) -> R): R = block(p1, p2, p3)

@Composable
inline fun <R> inlineCompose(block: @Composable () -> R): R = block()