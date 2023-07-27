package dev.flammky.valorantcompanion.live.loadout.presentation

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.debug.debugResourceUsage
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Background
import dev.flammky.valorantcompanion.live.BuildConfig
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.ceil

@Composable
fun SprayPickerPool(
    modifier: Modifier,
    ownedSprays: List<String>,
) {
   SubcomposeLayout(modifier) { constraints ->

       val placeable = subcompose(Unit) {
            SprayPickerPool(
                modifier = Modifier,
                density = LocalDensity.current,
                constraints = constraints,
                ownedSprays = ownedSprays
            )
       }.first().measure(constraints)

       layout(
           placeable.width, placeable.height
       ) {
           placeable.place(0, 0)
       }
   }
}

@Composable
fun SprayPickerPool(
    modifier: Modifier,
    density: Density,
    constraints: Constraints,
    ownedSprays: List<String>,
) {
    Box(
        modifier = modifier
            .then(
                remember(density, constraints) {
                    with(density) {
                        Modifier.sizeIn(
                            minWidth = constraints.minWidth.toDp(),
                            minHeight = constraints.minHeight.toDp(),
                            maxWidth = constraints.maxWidth.toDp(),
                            maxHeight = constraints.maxHeight.toDp()
                        )
                    }
                }
            )
            .fillMaxSize()
    ) {
        val totalCell = ownedSprays.size
        val minRowSpacing = 8.dp
        val rowSpacingPx = with(density) { minRowSpacing.roundToPx() }
        val minRowCellSpacing = 8.dp
        val rowCellSpacingPx = with(density) { minRowCellSpacing.roundToPx() }
        val cellMinSize = with(density) { SprayPickerPoolCellGroupMinSize.roundToPx() }
        val cellMaxSize = with(density) { SprayPickerPoolCellGroupMaxSize.roundToPx() }
        val (optimalCellSize, optimalCellSpacing) = run {
            val constraint = constraints.maxWidth
            val minSpacing = rowCellSpacingPx
            val minCellSize = cellMinSize
            val maxCellSize = cellMaxSize

            // Calculate the number of cells that can fit in the constraint without considering spacing
            val numberOfCells = (constraint / minCellSize)

            // Calculate the space occupied by the cells themselves (without spacing)
            val totalOccupiedSpace = numberOfCells * minCellSize

            // Calculate the total spacing required
            val totalSpacing = (numberOfCells - 1) * minSpacing

            // Calculate the total space occupied by cells and spacing
            val totalRequiredSpace = totalOccupiedSpace + totalSpacing

            // Calculate the extra space available beyond the required space
            val extraSpace = constraint - totalRequiredSpace

            // Calculate the cell size with extra space
            var cellSizeWithExtraSpace = minCellSize + extraSpace / numberOfCells

            // Check if the cell size exceeds the maximum cell size
            if (cellSizeWithExtraSpace > maxCellSize) {
                cellSizeWithExtraSpace = maxCellSize
                // Recalculate the extra spacing with the updated cell size
                val remainingSpace = constraint - (cellSizeWithExtraSpace * numberOfCells)
                val extraSpacing = (remainingSpace - (numberOfCells - 1) * minSpacing) / numberOfCells
                cellSizeWithExtraSpace to minSpacing + extraSpacing
            } else {
                // No need for extra spacing, return 0 as extraSpacing
                cellSizeWithExtraSpace to minSpacing
            }
        }
        val maxCellRowPerGroup = run {
            if (optimalCellSize == 0) return@run 0
            val maxHeight = constraints.maxHeight - optimalCellSize
            if (maxHeight < 0) return@run 0
            maxHeight / (optimalCellSize + rowSpacingPx) + 1
        }
        val maxCellInRow = run {
            if (optimalCellSize == 0) return@run 0
            val maxWidth = constraints.maxWidth - optimalCellSize
            if (maxWidth < 0) return@run 0
            maxWidth / (optimalCellSize + rowCellSpacingPx) + 1
        }
        SprayPickerPoolPager(
            modifier = Modifier,
            totalCell = totalCell,
            maxCellRowPerGroup = maxCellRowPerGroup,
            maxCellInRow = maxCellInRow,
            rowSpacing = minRowSpacing,
            cellSize = with(density) { optimalCellSize.toDp() },
            cellSpacing = with(density) { optimalCellSpacing.toDp() },
            getCellSpray = { index -> ownedSprays[index] }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SprayPickerPoolPager(
    modifier: Modifier,
    totalCell: Int,
    maxCellRowPerGroup: Int,
    maxCellInRow: Int,
    rowSpacing: Dp,
    cellSize: Dp,
    cellSpacing: Dp,
    getCellSpray: (Int) -> String
) {
    val groupCount = run {
        if (totalCell == 0) return@run 0
        val maxCellPerGroup = maxCellRowPerGroup * maxCellInRow
        if (totalCell <= maxCellPerGroup) return@run 1
        ceil(totalCell.toFloat() / maxCellPerGroup).toInt()
    }
    HorizontalPager(
        modifier = modifier,
        pageCount = groupCount,
        state = rememberPagerState(),
    ) { pageIndex ->
        val groupCellStartIndex = pageIndex * maxCellRowPerGroup * maxCellInRow
        val isLastGroup = pageIndex == groupCount -1
        val cellCountInGroup = if (isLastGroup) {
            totalCell - (maxCellInRow * maxCellRowPerGroup * (pageIndex))
        } else {
            maxCellInRow * maxCellRowPerGroup
        }
        val rowCount = ceil(cellCountInGroup.toFloat() / maxCellInRow).toInt()
        SprayPickerPoolCellGroup(
            modifier = Modifier.fillMaxSize(),
            rowCount = rowCount,
            getRowModifier = { Modifier },
            rowSpacing = rowSpacing,
            getRowCellCount = { rowIndex ->
                if (isLastGroup && rowIndex == rowCount - 1) {
                    cellCountInGroup - (maxCellInRow * (rowCount - 1))
                } else {
                    maxCellInRow
                }
            },
            rowCellSize = cellSize,
            rowCellSpacing = cellSpacing,
            getCellSpray = { indexInGroup -> getCellSpray(groupCellStartIndex + indexInGroup) }
        )
    }
}

@Composable
private fun SprayPickerPoolCellGroup(
    modifier: Modifier,
    rowCount: Int,
    getRowModifier: (Int) -> Modifier,
    rowSpacing: Dp,
    getRowCellCount: (Int) -> Int,
    rowCellSize: Dp,
    rowCellSpacing: Dp,
    getCellSpray: (Int) -> String
) = Column(modifier.fillMaxSize()) {

    Log.d(
        BuildConfig.LIBRARY_PACKAGE_NAME,
        "live.loadout.presentation.SprayPickerPoolKt.SprayPickerPoolCellGroup(rowCount=$rowCount, rowSpacing=$rowSpacing, rowCelLSize=$rowCellSize, rowCellSpacing=$rowCellSpacing)"
    )

    repeat(rowCount) { rowIndex ->

        Row(getRowModifier.invoke(rowIndex)) {
            val rowCellCount = getRowCellCount(rowIndex)
            repeat(rowCellCount) { cellIndex ->
                val indexInGroup = rowIndex * (rowCellCount - 1) + cellIndex
                SprayPickerPoolCell(
                    modifier = Modifier.size(rowCellSize),
                    cellSpray = getCellSpray.invoke(indexInGroup)
                )
                if (cellIndex < rowCellCount - 1) {
                    Spacer(modifier = Modifier.width(rowCellSpacing))
                }
            }
        }

        if (rowIndex < rowCount - 1) {
            Spacer(modifier = Modifier.height(rowSpacing))
        }
    }
}

@Composable
private fun SprayPickerPoolCell(
    modifier: Modifier,
    cellSpray: String
) {
    val ctx = LocalContext.current
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        model = remember(cellSpray, ctx) {
            // TODO: Load Spray from AssetLoader
            ImageRequest.Builder(ctx)
                .data(
                    debugResourceUsage {
                        R_ASSET_RAW.debug_spray_nice_to_zap_you_transparent
                    }
                )
                .build()
        },
        contentDescription = null
    )
}

private val SprayPickerPoolCellGroupMinSize = 80.dp
private val SprayPickerPoolCellGroupMaxSize = 120.dp

// TODO: move to debug module variant

@Composable
@Preview
private fun SprayPickerPoolPreview() {
    DefaultMaterial3Theme(
        dark = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .localMaterial3Background()
        ) {
            SprayPickerPool(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(0.4f)
                    .align(Alignment.BottomCenter),
                ownedSprays = persistentListOf<String>().builder()
                    .apply {
                        repeat(17) { index ->
                            add(index.toString())
                        }
                    }
                    .build()
            )
        }
    }
}

