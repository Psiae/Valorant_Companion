package dev.flammky.valorantcompanion.base.theme.material3

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal const val SPACER_INCREMENTS_VALUE = 24

fun Material3Theme.spacerIncrements(
    spacer: Dp,
    n: Int
): Dp = spacer + dpSpacerIncrementsOf(n)

fun Material3Theme.dpSpacerIncrementsOf(
    n: Int
): Dp = (SPACER_INCREMENTS_VALUE * n).dp