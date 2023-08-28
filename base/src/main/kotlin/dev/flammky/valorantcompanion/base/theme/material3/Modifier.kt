package dev.flammky.valorantcompanion.base.theme.material3

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.compose.consumeDownGesture
import dev.flammky.valorantcompanion.base.compose.ifUnspecified
import dev.flammky.valorantcompanion.base.uxsystem.*
import kotlin.math.ln

fun Modifier.localMaterial3Background(
    transform: (Color) -> Color
) = composed {
    background(transform(Material3Theme.backgroundColorAsState().value))
}

fun Modifier.localMaterial3Background() = composed {
    background(Material3Theme.backgroundColorAsState().value)
}

fun Modifier.localMaterial3Surface(
    color: (Color) -> Color,
    applyInteractiveUxEnforcement: Boolean = true,
    tonalElevation: Dp = 0.dp,
    tonalTint: Color = Color.Unspecified,
    shadowElevation: Dp = 0.dp,
    shape: Shape = RectangleShape,
    borderStroke: BorderStroke? = null,
) = composed {
    localMaterial3Surface(
        color = Material3Theme.surfaceColorAsState(transform = color).value,
        applyInteractiveUxEnforcement = applyInteractiveUxEnforcement,
        tonalElevation = tonalElevation,
        tonalTint = tonalTint,
        shadowElevation = shadowElevation,
        shape = shape,
        borderStroke = borderStroke,
    )
}

fun Modifier.localMaterial3Surface(
    color: Color = Color.Unspecified,
    applyInteractiveUxEnforcement: Boolean = true,
    tonalElevation: Dp = 0.dp,
    tonalTint: Color = Color.Unspecified,
    shadowElevation: Dp = 0.dp,
    shape: Shape = RectangleShape,
    borderStroke: BorderStroke? = null,
) = this
    .then(
        if (applyInteractiveUxEnforcement)
            Modifier.interactiveUiElementSizeEnforcement()
        else
            Modifier
    )
    .shadow(
        shadowElevation,
        shape,
        clip = false
    )
    .then(
        if (borderStroke != null)
            Modifier.border(borderStroke, shape)
        else
            Modifier
    )
    .then(
        composed {
            val specifiedColor = color.ifUnspecified { Material3Theme.surfaceColorAsState().value }
            Modifier.background(
                color = if (!tonalTint.isUnspecified)
                    surfaceColorAtElevation(
                        surface = specifiedColor,
                        tint = tonalTint,
                        elevation = tonalElevation
                    )
                else
                    specifiedColor
            )
        }
    )
    .clip(shape)
    .consumeDownGesture()

fun Modifier.interactiveUiElementSizeEnforcement() = sizeIn(
    minWidth = MATERIAL3_INTERACTIVE_COMPONENT_MINIMUM_SIZE_DP.dp,
    minHeight = MATERIAL3_INTERACTIVE_COMPONENT_MINIMUM_SIZE_DP.dp
)

fun Modifier.interactiveUiElementAlphaEnforcement(
    isContent: Boolean,
    enabled: Boolean
) = alpha(
    alpha = when {
        enabled -> 1f
        isContent -> MATERIAL3_INTERACTIVE_COMPONENT_SURFACE_CONTENT_DISABLED_ALPHA
        else -> MATERIAL3_INTERACTIVE_COMPONENT_SURFACE_DISABLED_ALPHA
    }
)

fun Modifier.interactiveTextUiElementAlphaEnforcement(
    isContent: Boolean,
    enabled: Boolean
) = alpha(
    alpha = when {
        enabled -> 1f
        isContent -> MATERIAL3_INTERACTIVE_TEXT_COMPONENT_SURFACE_CONTENT_DISABLED_ALPHA
        else -> MATERIAL3_INTERACTIVE_TEXT_COMPONENT_SURFACE_DISABLED_ALPHA
    }
)

private fun surfaceColorAtElevation(
    surface: Color,
    tint: Color,
    elevation: Dp,
): Color {
    if (elevation == 0.dp) return surface
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return tint.copy(alpha = alpha).compositeOver(surface)
}