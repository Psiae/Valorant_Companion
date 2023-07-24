package dev.flammky.valorantcompanion.live.loadout.presentation

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.commonkt.geometry.*
import dev.flammky.valorantcompanion.base.compose.geometry.plus
import dev.flammky.valorantcompanion.base.compose.geometry.roundToIntOffset
import dev.flammky.valorantcompanion.base.debug.DebugResource
import dev.flammky.valorantcompanion.base.theme.material3.*
import kotlin.math.PI
import kotlin.math.roundToInt

@Composable
fun SprayPicker(
    modifier: Modifier,
    activeSpraySlotCount: Int,
    getSpray: (Int) -> String
) {
    check(activeSpraySlotCount > 0) {
        "activeSpraySlotCount must be more than 0"
    }
    SubcomposeLayout(modifier) { parentConstraints ->
        val constraints = minOf(parentConstraints.maxWidth, parentConstraints.maxHeight).let { size ->
            Constraints(maxWidth = size, maxHeight = size)
        }

        val center = Size(
            constraints.maxWidth.toFloat() / 2,
            constraints.maxHeight.toFloat() / 2
        )

        val placeable = subcompose(Unit) {
            SprayPickerLayout(
                total = activeSpraySlotCount,
                getSpray = getSpray,
                constraints = constraints
            )
        }.first().measure(constraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            val circleRadius = minOf(center.width, center.height)
            placeable.place(
                x = circleRadius.roundToInt() - placeable.width / 2,
                y = circleRadius.roundToInt() - placeable.height / 2,
                zIndex = 0f
            )
        }
    }
}

@Composable
private fun SprayPickerLayout(
    total: Int,
    getSpray: (Int) -> String,
    constraints: Constraints
) = with(LocalDensity.current) {

    if (total <= 0) {
        TODO("show no spray layout")
    }

    if (total == 1) {
        TODO("show single spray layout")
    }

    Box(
        modifier = Modifier
            .size(constraints.maxWidth.toDp(), constraints.maxHeight.toDp())
    ) {
        val circleRadius = minOf(
            constraints.maxWidth.toFloat() / 2,
            constraints.maxHeight.toFloat() / 2
        )
        val innerCircleRadius = 0.3f * circleRadius
        SprayPickerCells(total, getSpray, circleRadius, innerCircleRadius, dividerThickness = 5.dp)
        SprayPickerCellsDivider(total, circleRadius, innerCircleRadius, thickness = 5.dp)
    }
}

@Composable
private fun SprayPickerCells(
    total: Int,
    getSpray: (Int) -> String,
    circleRadius: Float,
    innerCircleRadius: Float,
    dividerThickness: Dp
) = repeat(total) { index ->
    SprayPickerCell(
        total = total,
        index = index,
        spray = getSpray(index),
        circleRadius = circleRadius,
        innerCircleRadius = innerCircleRadius,
        dividerThickness = dividerThickness
    )
}

@Composable
private fun SprayPickerCell(
    total: Int,
    index: Int,
    spray: String,
    circleRadius: Float,
    innerCircleRadius: Float,
    dividerThickness: Dp
) {
    val cellSize = 0.8f * (circleRadius - innerCircleRadius)
    val density = LocalDensity.current
    val angle = remember(index, total) { 2 * PI * (index.toFloat() / total) }
    val angleOffset = remember(total) { PI / total }
    val startRadians = (angle - angleOffset).toFloat()
    val endRadians = (angle + angleOffset).toFloat()
    val paddingRadians = if (dividerThickness.value > 0) {
        val padding = with(density) { (dividerThickness + 5.dp).toPx() }
        val circumference = 2 * PI.toFloat() * circleRadius
        padding / circumference * (2 * PI.toFloat())
    } else {
        0f
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sprayPickerCellOutline(
                color = Material3Theme.surfaceVariantContentColorAsState().value,
                circleRadius = circleRadius,
                innerCircleRadius = innerCircleRadius,
                startRadians = startRadians,
                endRadians = endRadians,
                paddingRadians = paddingRadians,
                thickness = 2.dp
            )
            .sprayPickerCellClipShape(
                circleRadius = circleRadius,
                innerCircleRadius = innerCircleRadius,
                startRadians = startRadians,
                endRadians = endRadians,
                paddingRadians = paddingRadians,
                padding = 8.dp
            )
            .sprayPickerCellLayer()
            .clickable { }
    ) {
        if (spray.isNotEmpty()) {
            // TODO: load spray
            AsyncImage(
                modifier = Modifier
                    .run {
                        with(LocalDensity.current) {
                            Modifier.size(cellSize.toDp())
                        }
                    }
                    .offset {
                        val center = Offset(circleRadius, circleRadius)
                        val length = (circleRadius + innerCircleRadius) / 2
                        val angle = Angle.Radians(2 * PI * (index.toFloat() / total))
                        val cellAlignment = Offset(
                            x = cellSize / 2,
                            y = cellSize / 2
                        )
                        val offset = center + vector2D(angle, length) - cellAlignment
                        offset.roundToIntOffset()
                    },
                model = run {
                    val ctx = LocalContext.current
                    remember {
                        ImageRequest.Builder(ctx)
                            .data(
                                with(DebugResource) { R_ASSET_RAW.nice_to_zap_you_transparent }
                            )
                            .build()
                    }
                },
                contentDescription = "agent Icon"
            )
        }
    }
}

private fun Path.drawSprayPickerCellFrame(
    circleRadius: Float,
    innerCircleRadius: Float,
    startRadians: Float,
    endRadians: Float,
    paddingRadians: Float,
    topPadding: Float,
    bottomPadding: Float
): Path {

    sprayPickerCellOuterFrameArc(
        circleRadius = circleRadius,
        startAngleRadians = startRadians,
        endAngleRadians = endRadians,
        paddingRadians = paddingRadians,
        padding = topPadding
    )

    sprayPickerCellInnerFrameArc(
        circleRadius = circleRadius,
        innerCircleRadius = innerCircleRadius,
        angleStartRadians = startRadians,
        angleEndRadians = endRadians,
        paddingRadians = paddingRadians,
        padding = bottomPadding
    )

    close()

    return this
}

private fun Path.sprayPickerCellOuterFrameArc(
    circleRadius: Float,
    startAngleRadians: Float,
    endAngleRadians: Float,
    paddingRadians: Float,
    padding: Float
): Path {
    val circleDiameter = circleRadius.times(2)
    val startAngle = Angle.Radians(startAngleRadians + paddingRadians)
    val endAngle = Angle.Radians(endAngleRadians - paddingRadians)
    arcTo(
        rect = Rect(
            0 + padding,
            0 + padding,
            circleDiameter - padding,
            circleDiameter - padding
        ),
        startAngleDegrees = startAngle.degree,
        sweepAngleDegrees = endAngle.degree - startAngle.degree,
        forceMoveTo = false
    )
    return this
}

private fun Path.sprayPickerCellInnerFrameArc(
    circleRadius: Float,
    innerCircleRadius: Float,
    angleStartRadians: Float,
    angleEndRadians: Float,
    paddingRadians: Float,
    padding: Float
): Path {
    val center = Offset(circleRadius, circleRadius)
    val startInnerAngle = Angle.Radians(angleEndRadians - paddingRadians * 3)
    val endInnerAngle = Angle.Radians(angleStartRadians + paddingRadians * 3)
    val rect = Rect(
        center.x - innerCircleRadius - padding,
        center.y - innerCircleRadius - padding,
        center.x + innerCircleRadius + padding,
        center.y + innerCircleRadius + padding
    )
    val startAngleDegrees = startInnerAngle.degree
    val sweepAngleDegrees = endInnerAngle.degree - startInnerAngle.degree
    Log.d("live.loadout.SprayPickerKt", "sprayPickerCellInnerFrameArc($circleRadius, $angleStartRadians, $angleEndRadians, $paddingRadians); ($center, $startInnerAngle, $endInnerAngle)")
    arcTo(
        rect = rect,
        startAngleDegrees = startAngleDegrees,
        sweepAngleDegrees = sweepAngleDegrees,
        forceMoveTo = false
    )
    return this
}

private fun Modifier.sprayPickerCellClipShape(
    circleRadius: Float,
    innerCircleRadius: Float,
    startRadians: Float,
    endRadians: Float,
    paddingRadians: Float,
    padding: Dp
): Modifier = composed {
    val density = LocalDensity.current
    clip(
        GenericShape(
            builder = { _: Size, _: LayoutDirection ->
                val paddingPx = with(density) { padding.toPx() }
                drawSprayPickerCellFrame(
                    circleRadius = circleRadius,
                    innerCircleRadius = innerCircleRadius,
                    startRadians = startRadians,
                    endRadians = endRadians,
                    paddingRadians = paddingRadians,
                    topPadding = paddingPx,
                    bottomPadding = paddingPx
                )
            }
        )
    )
}

private fun Modifier.sprayPickerCellOutline(
    color: Color,
    circleRadius: Float,
    innerCircleRadius: Float,
    startRadians: Float,
    endRadians: Float,
    paddingRadians: Float,
    thickness: Dp
): Modifier {
    return this
        .sprayPickerCellOuterOutline(
            color,
            circleRadius,
            startRadians,
            endRadians,
            paddingRadians,
            thickness
        )
        .sprayPickerCellInnerOutline(
            color,
            circleRadius,
            innerCircleRadius,
            startRadians,
            endRadians,
            paddingRadians,
            thickness
        )
}

private fun Modifier.sprayPickerCellOuterOutline(
    color: Color,
    circleRadius: Float,
    angleStartRadians: Float,
    angleEndRadians: Float,
    paddingRadians: Float,
    thickness: Dp
): Modifier = drawBehind {
    drawPath(
        path = Path()
            .sprayPickerCellOuterFrameArc(
                circleRadius,
                angleStartRadians,
                angleEndRadians,
                paddingRadians,
                0f
            ),
        color = color,
        style = Stroke(
            width = thickness.toPx(),
            cap = StrokeCap.Butt
        )
    )
}

private fun Modifier.sprayPickerCellInnerOutline(
    color: Color,
    circleRadius: Float,
    innerCircleRadius: Float,
    angleStartRadians: Float,
    angleEndRadians: Float,
    paddingRadians: Float,
    thickness: Dp
): Modifier = drawBehind {
    drawPath(
        path = Path()
            .sprayPickerCellInnerFrameArc(
                circleRadius = circleRadius,
                innerCircleRadius = innerCircleRadius,
                angleStartRadians = angleStartRadians,
                angleEndRadians = angleEndRadians,
                paddingRadians = paddingRadians,
                padding = 0f
            ),
        color = color,
        style = Stroke(
            width = thickness.toPx(),
            cap = StrokeCap.Butt
        )
    )
}

private fun Modifier.sprayPickerCellLayer(

): Modifier = composed {
    val surface = Material3Theme.surfaceVariantColorAsState().value
    remember(surface) {
        background(surface.copy(alpha = 0.74f))
    }
}

@Composable
private fun SprayPickerCellsDivider(
    total: Int,
    circleRadius: Float,
    innerCircleRadius: Float,
    thickness: Dp = 5.dp
) {
    if (total > 1) repeat(total) { index ->
        SprayPickerCellDivider(
            index = index,
            total = total,
            circleRadius = circleRadius,
            innerCircleRadius = innerCircleRadius,
            thickness = thickness
        )
    }
}

@Composable
private fun SprayPickerCellDivider(
    index: Int,
    total: Int,
    circleRadius: Float,
    innerCircleRadius: Float,
    thickness: Dp
) {
    val angle = Angle.Radians(2 * PI * (index.toFloat() / total))
    val angleOffset = Angle.Radians(PI / total)
    val lineColor = Material3Theme.surfaceVariantContentColorAsState().value
    val width = 1.2f * circleRadius - innerCircleRadius
    Canvas(
        modifier = Modifier
            .width(with(LocalDensity.current) {
                width.toDp()
            })
            .height(thickness)
            .offset {
                val center = Offset(circleRadius, circleRadius)
                val length = (circleRadius + innerCircleRadius) / 2
                val cellAlignment = Offset(
                    x = width / 2,
                    y = thickness.toPx() / 2
                )
                val offset = center + vector2D(angle + angleOffset, length) - cellAlignment
                offset.roundToIntOffset()
            }
            .graphicsLayer {
                rotationZ = (angle + angleOffset).degree
            },
        onDraw = {
            drawLine(
                color = lineColor,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = thickness.value,
                cap = StrokeCap.Butt,
            )
        }
    )
}


@Preview
@Composable
private fun SprayPickerPreview() {
    DefaultMaterial3Theme(dark = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Material3Theme.backgroundColorAsState().value)
        ) {
            SprayPicker(
                modifier = Modifier,
                activeSpraySlotCount = 4,
                getSpray = { it.toString() }
            )
        }
    }
}