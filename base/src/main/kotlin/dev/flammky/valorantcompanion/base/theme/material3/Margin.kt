package dev.flammky.valorantcompanion.base.theme.material3

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal const val MARGIN_INCREMENTS_VALUE_COMPACT = 16

internal const val MARGIN_INCREMENTS_VALUE_MEDIUM = 24

internal const val MARGIN_INCREMENTS_VALUE_EXPANDED = 24

fun Material3Theme.marginIncrements(
    other: Dp,
    n: Int,
    widthConstraints: Dp
): Dp = other + dpMarginIncrementsOf(n, widthConstraints)

fun Material3Theme.dpMarginIncrementsOf(
    n: Int,
    widthConstraints: Dp
): Dp = when {
    widthConstraints.value <= 0 -> 0.dp
    widthConstraints.value <= WindowSize.COMPACT.maxWidthDp -> MARGIN_INCREMENTS_VALUE_COMPACT.dp
    widthConstraints.value <= WindowSize.MEDIUM.maxWidthDp -> MARGIN_INCREMENTS_VALUE_MEDIUM.dp
    else -> MARGIN_INCREMENTS_VALUE_EXPANDED.dp
} * n