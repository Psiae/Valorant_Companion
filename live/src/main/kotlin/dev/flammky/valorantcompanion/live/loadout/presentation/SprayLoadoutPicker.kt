package dev.flammky.valorantcompanion.live.loadout.presentation

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.fastForEach
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.spray.LoadSprayImageRequest
import dev.flammky.valorantcompanion.assets.spray.ValorantSprayImageType
import dev.flammky.valorantcompanion.base.commonkt.geometry.*
import dev.flammky.valorantcompanion.base.compose.geometry.plus
import dev.flammky.valorantcompanion.base.compose.geometry.roundToIntOffset
import dev.flammky.valorantcompanion.base.compose.rememberWithCompositionObserver
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.requireInject
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.pvp.PvpConstants
import dev.flammky.valorantcompanion.pvp.spray.pvpSpraySlotUUIDOf4CircularIndex
import kotlin.math.PI
import kotlin.math.roundToInt

@Composable
fun SprayLoadoutPicker(
    modifier: Modifier,
    state: SprayLoadoutPickerState
) {
    SprayLoadoutPicker(
        modifier = modifier,
        activeSpraySlotCount = PvpConstants.SPRAY_SLOT_COUNT,
        activeSprayCount = state.activeSprays.size,
        getSpray = remember(state.activeSpraysKey) {
            { index ->
                state.activeSprays.find { it.equipSlotId == pvpSpraySlotUUIDOf4CircularIndex(index) }
                    ?.sprayId
                    ?: ""
            }
        }
    )
}

@Composable
fun SprayLoadoutPicker(
    modifier: Modifier,
    activeSpraySlotCount: Int,
    activeSprayCount: Int,
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
                getSpray = remember(getSpray) {
                    { i -> if (i < activeSprayCount) getSpray(i) else null }
                },
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
    getSpray: (Int) -> String?,
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
    getSpray: (Int) -> String?,
    circleRadius: Float,
    innerCircleRadius: Float,
    dividerThickness: Dp
) = repeat(total) { index ->
    SprayPickerCell(
        total = total,
        index = index,
        circleRadius = circleRadius,
        innerCircleRadius = innerCircleRadius,
        dividerThickness = dividerThickness,
        Content = { modifier -> SprayPickerCellContent(modifier = modifier, spray = getSpray(index)) },
    )
}

@Composable
private fun SprayPickerCell(
    total: Int,
    index: Int,
    circleRadius: Float,
    innerCircleRadius: Float,
    dividerThickness: Dp,
    Content: @Composable BoxScope.(modifier: Modifier) -> Unit
) = Layout(
    modifier = Modifier
        .fillMaxSize()
        .sprayPickerCellLayoutModifiers(
            density = LocalDensity.current,
            total = total,
            index = index,
            circleRadius = circleRadius,
            innerCircleRadius = innerCircleRadius,
            dividerThickness = dividerThickness
        )
        .clickable { },
    content = {
        Box {
            Content(
                modifier = Modifier
                    .sprayPickerCellContentLayoutModifiers(
                        density = LocalDensity.current,
                        index = index,
                        total = total,
                        circleRadius = circleRadius,
                        innerCircleRadius = innerCircleRadius
                    )
            )
        }
    },
    measurePolicy = { measurables, constraint ->
        layout(constraint.maxWidth, constraint.maxHeight) {
            measurables.fastForEach { it.measure(constraint).place(0, 0) }
        }
    }
)

@Composable
fun SprayPickerCellContent(
    modifier: Modifier,
    spray: String?
) {
// TODO: use content lambda
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        model = run {
            // TODO: make into se
            val ctx = LocalContext.current
            val assetLoaderService =
                LocalDependencyInjector
                    .current
                    .requireInject<ValorantAssetsService>()
            val assetLoaderClient =
                rememberWithCompositionObserver(
                    key = assetLoaderService,
                    onRemembered = { client -> ; },
                    onForgotten = { client -> client.dispose() },
                    onAbandoned = { client -> client.dispose() },
                    block = { assetLoaderService.createLoaderClient() }
                )
            val dataState = remember(spray) {
                mutableStateOf<Any?>(null)
            }
            LaunchedEffect(
                key1 = spray,
                key2 = assetLoaderClient,
            ) {
                if (spray == null) return@LaunchedEffect
                val def = assetLoaderClient.loadSprayImageAsync(
                    req = LoadSprayImageRequest(
                        uuid = spray,
                        ValorantSprayImageType.FULL_ICON(transparentBackground = true),
                        ValorantSprayImageType.FULL_ICON(transparentBackground = false),
                        ValorantSprayImageType.DISPLAY_ICON
                    )
                )
                val image = runCatching { def.await() }.onFailure { def.cancel() }.getOrThrow()
                // TODO: if it fails ask to refresh
                dataState.value = image.getOrNull()?.value
            }
            remember(dataState.value) {
                ImageRequest.Builder(ctx)
                    .data(dataState.value)
                    .build()
            }
        },
        contentDescription = "agent Icon"
    )
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
    density: Density,
    circleRadius: Float,
    innerCircleRadius: Float,
    startRadians: Float,
    endRadians: Float,
    paddingRadians: Float,
    padding: Dp
): Modifier = clip(
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

private fun Modifier.sprayPickerCellLayoutModifiers(
    density: Density,
    total: Int,
    index: Int,
    circleRadius: Float,
    innerCircleRadius: Float,
    dividerThickness: Dp
) = composed {
    val outlineColor = Material3Theme.surfaceVariantContentColorAsState().value
    remember(
        total,
        index,
        circleRadius,
        innerCircleRadius,
        dividerThickness,
        outlineColor,
        density
    ) {
        val angle =  2 * PI * (index.toFloat() / total)
        val angleOffset = PI / total
        val startRadians = (angle - angleOffset).toFloat()
        val endRadians = (angle + angleOffset).toFloat()
        val paddingRadians = if (dividerThickness.value > 0) {
            val padding = with(density) { (dividerThickness + 5.dp).toPx() }
            val circumference = 2 * PI.toFloat() * circleRadius
            padding / circumference * (2 * PI.toFloat())
        } else {
            0f
        }
        Modifier
            .sprayPickerCellOutline(
                color = outlineColor,
                circleRadius = circleRadius,
                innerCircleRadius = innerCircleRadius,
                startRadians = startRadians,
                endRadians = endRadians,
                paddingRadians = paddingRadians,
                thickness = 2.dp
            )
            .sprayPickerCellClipShape(
                density = density,
                circleRadius = circleRadius,
                innerCircleRadius = innerCircleRadius,
                startRadians = startRadians,
                endRadians = endRadians,
                paddingRadians = paddingRadians,
                padding = 8.dp
            )
            .sprayPickerCellLayer()
    }
}

private fun Modifier.sprayPickerCellContentLayoutModifiers(
    density: Density,
    index: Int,
    total: Int,
    circleRadius: Float,
    innerCircleRadius: Float,
) = composed {
    remember(density, index, total, circleRadius, innerCircleRadius) {
        val cellSize = 0.8f * (circleRadius - innerCircleRadius)
        Modifier
            .size(with(density) { cellSize.toDp() })
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
            }
    }
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
            SprayLoadoutPicker(
                modifier = Modifier,
                activeSpraySlotCount = 4,
                activeSprayCount = 4,
                getSpray = { it.toString() },
            )
        }
    }
}