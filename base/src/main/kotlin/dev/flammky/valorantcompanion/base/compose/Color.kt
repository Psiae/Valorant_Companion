package dev.flammky.valorantcompanion.base.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.luminance
import kotlin.math.nextUp

inline fun Color.ifUnspecified(
    color: () -> Color
): Color = if (isUnspecified) color() else this

fun Color.nearestBlackOrWhite(
    whiteLuminanceThreshold: Float = 0.5f.nextUp()
) = if (luminance() >= whiteLuminanceThreshold) Color.White else Color.Black