package dev.flammky.valorantcompanion.base.theme.material3

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val Material3Theme.PADDING_INCREMENTS_VALUE
    get() = 4

fun Material3Theme.paddingIncrements(
    padding: Dp,
    n: Int
): Dp = padding + dpPaddingIncrementsOf(n)

fun Material3Theme.dpPaddingIncrementsOf(
    n: Int
): Dp = (PADDING_INCREMENTS_VALUE * n).dp