package dev.flammky.valorantcompanion.live.loadout.presentation.uselibinternals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.NoInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.max

@PublishedApi
@Composable
internal fun rememberBoxMeasurePolicy(
    alignment: Alignment,
    propagateMinConstraints: Boolean
) = if (alignment == Alignment.TopStart && !propagateMinConstraints) {
    DefaultBoxMeasurePolicy
} else {
    remember(alignment, propagateMinConstraints) {
        boxMeasurePolicy(alignment, propagateMinConstraints)
    }
}

internal val DefaultBoxMeasurePolicy: MeasurePolicy = boxMeasurePolicy(Alignment.TopStart, false)

internal fun boxMeasurePolicy(alignment: Alignment, propagateMinConstraints: Boolean) =
    MeasurePolicy { measurables, constraints ->
        if (measurables.isEmpty()) {
            return@MeasurePolicy layout(
                constraints.minWidth,
                constraints.minHeight
            ) {}
        }

        val contentConstraints = if (propagateMinConstraints) {
            constraints
        } else {
            constraints.copy(minWidth = 0, minHeight = 0)
        }

        if (measurables.size == 1) {
            val measurable = measurables[0]
            val boxWidth: Int
            val boxHeight: Int
            val placeable: Placeable
            if (!measurable.matchesParentSize) {
                placeable = measurable.measure(contentConstraints)
                boxWidth = max(constraints.minWidth, placeable.width)
                boxHeight = max(constraints.minHeight, placeable.height)
            } else {
                boxWidth = constraints.minWidth
                boxHeight = constraints.minHeight
                placeable = measurable.measure(
                    Constraints.fixed(constraints.minWidth, constraints.minHeight)
                )
            }
            return@MeasurePolicy layout(boxWidth, boxHeight) {
                placeInBox(placeable, measurable, layoutDirection, boxWidth, boxHeight, alignment)
            }
        }

        val placeables = arrayOfNulls<Placeable>(measurables.size)
        // First measure non match parent size children to get the size of the Box.
        var hasMatchParentSizeChildren = false
        var boxWidth = constraints.minWidth
        var boxHeight = constraints.minHeight
        measurables.fastForEachIndexed { index, measurable ->
            if (!measurable.matchesParentSize) {
                val placeable = measurable.measure(contentConstraints)
                placeables[index] = placeable
                boxWidth = max(boxWidth, placeable.width)
                boxHeight = max(boxHeight, placeable.height)
            } else {
                hasMatchParentSizeChildren = true
            }
        }

        // Now measure match parent size children, if any.
        if (hasMatchParentSizeChildren) {
            // The infinity check is needed for default intrinsic measurements.
            val matchParentSizeConstraints = Constraints(
                minWidth = if (boxWidth != Constraints.Infinity) boxWidth else 0,
                minHeight = if (boxHeight != Constraints.Infinity) boxHeight else 0,
                maxWidth = boxWidth,
                maxHeight = boxHeight
            )
            measurables.fastForEachIndexed { index, measurable ->
                if (measurable.matchesParentSize) {
                    placeables[index] = measurable.measure(matchParentSizeConstraints)
                }
            }
        }

        // Specify the size of the Box and position its children.
        layout(boxWidth, boxHeight) {
            placeables.forEachIndexed { index, placeable ->
                placeable as Placeable
                val measurable = measurables[index]
                placeInBox(placeable, measurable, layoutDirection, boxWidth, boxHeight, alignment)
            }
        }
    }

private fun Placeable.PlacementScope.placeInBox(
    placeable: Placeable,
    measurable: Measurable,
    layoutDirection: LayoutDirection,
    boxWidth: Int,
    boxHeight: Int,
    alignment: Alignment
) {
    val childAlignment = measurable.boxChildData?.alignment ?: alignment
    val position = childAlignment.align(
        IntSize(placeable.width, placeable.height),
        IntSize(boxWidth, boxHeight),
        layoutDirection
    )
    placeable.place(position)
}


private val Measurable.boxChildData: BoxChildData? get() = parentData as? BoxChildData
private val Measurable.matchesParentSize: Boolean get() = boxChildData?.matchParentSize ?: false

private class BoxChildData(
    var alignment: Alignment,
    var matchParentSize: Boolean = false,
    inspectorInfo: InspectorInfo.() -> Unit = NoInspectorInfo
) : ParentDataModifier, InspectorValueInfo(inspectorInfo) {
    override fun Density.modifyParentData(parentData: Any?) = this@BoxChildData

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? BoxChildData ?: return false

        return alignment == otherModifier.alignment &&
                matchParentSize == otherModifier.matchParentSize
    }

    override fun hashCode(): Int {
        var result = alignment.hashCode()
        result = 31 * result + matchParentSize.hashCode()
        return result
    }

    override fun toString(): String =
        "BoxChildData(alignment=$alignment, matchParentSize=$matchParentSize)"
}