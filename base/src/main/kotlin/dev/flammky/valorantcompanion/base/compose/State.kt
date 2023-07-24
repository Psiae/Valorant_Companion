package dev.flammky.valorantcompanion.base.compose

import androidx.compose.runtime.*

@Composable
fun <T> rememberUpdatedState(
    newValue: T,
    mutationPolicy: SnapshotMutationPolicy<T>
): State<T> = remember(mutationPolicy) {
    mutableStateOf(newValue, mutationPolicy)
}.apply { value = newValue }