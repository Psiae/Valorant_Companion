package dev.flammky.valorantcompanion.base.compose

import androidx.compose.runtime.*

// should we put noinline ?

@Composable
inline fun <R> composeWithKeyArgs(
    vararg keys: Any?,
    noinline block: @Composable (keys: Array<out Any?>) -> R
): R = compose {
    val state = rememberUpdatedState(newValue = keys)
    block(state.value)
}

@Composable
inline fun <R, T1> composeWithKey(
    key1: T1,
    noinline block: @Composable (T1) -> R
): R = compose {
    val s1 = rememberUpdatedState(newValue = key1)
    block(s1.value)
}

@Composable
inline fun <R, T1, T2,> composeWithKey(
    key1: T1,
    key2: T2,
    noinline block: @Composable (T1, T2) -> R
): R = compose {
    val s1 = rememberUpdatedState(newValue = key1)
    val s2 = rememberUpdatedState(newValue = key2)
    block(s1.value, s2.value)
}

@Composable
inline fun <R, T1, T2, T3> composeWithKey(
    key1: T1,
    key2: T2,
    key3: T3,
    noinline block: @Composable (T1, T2, T3) -> R
): R = compose {
    val s1 = rememberUpdatedState(newValue = key1)
    val s2 = rememberUpdatedState(newValue = key2)
    val s3 = rememberUpdatedState(newValue = key3)
    block(s1.value, s2.value, s3.value)
}


@Composable
inline fun <R, T1, T2, T3, T4> composeWithKey(
    key1: T1,
    key2: T2,
    key3: T3,
    key4: T4,
    noinline block: @Composable (T1, T2, T3, T4) -> R
): R = compose {
    val s1 = rememberUpdatedState(newValue = key1)
    val s2 = rememberUpdatedState(newValue = key2)
    val s3 = rememberUpdatedState(newValue = key3)
    val s4 = rememberUpdatedState(newValue = key4)
    block(s1.value, s2.value, s3.value, s4.value)
}

@Composable
inline fun <R, T1, T2, T3, T4, T5> composeWithKey(
    key1: T1,
    key2: T2,
    key3: T3,
    key4: T4,
    key5: T5,
    noinline block: @Composable (T1, T2, T3, T4, T5) -> R
): R = compose {
    val s1 = rememberUpdatedState(newValue = key1)
    val s2 = rememberUpdatedState(newValue = key2)
    val s3 = rememberUpdatedState(newValue = key3)
    val s4 = rememberUpdatedState(newValue = key4)
    val s5 = rememberUpdatedState(newValue = key5)
    block(s1.value, s2.value, s3.value, s4.value, s5.value)
}