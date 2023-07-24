package dev.flammky.valorantcompanion.live.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceColorAsState
import dev.flammky.valorantcompanion.base.theme.material3.surfaceContentColorAsState
import dev.flammky.valorantcompanion.base.theme.material3.surfaceVariantColorAsState

@Composable
internal fun LiveMainContentTopTab(
    modifier: Modifier,
    selectedTabIndex: Int,
    selectTab: (Int) -> Unit
) = TabRow(
    modifier = modifier.fillMaxWidth(),
    selectedTabIndex = selectedTabIndex,
    backgroundColor = Material3Theme.surfaceColorAsState().value,
    contentColor = Material3Theme.surfaceContentColorAsState().value,
    indicator = @Composable { tabPositions ->
        Box(
            Modifier
                .composed {
                    val tabPosition =
                        tabPositions[selectedTabIndex]
                    val indicatorWidth by animateDpAsState(
                        targetValue = tabPosition.width * 0.8f,
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                    )
                    val indicatorOffset by animateDpAsState(
                        targetValue = (tabPosition.left + (tabPosition.width / 2 - (indicatorWidth / 2)))
                            .coerceAtLeast(tabPosition.left),
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                    )
                    this
                        .wrapContentSize(Alignment.BottomStart)
                        .offset(x = indicatorOffset)
                        .width(indicatorWidth)
                        .height(3.dp)
                        .background(Material3Theme.surfaceVariantColorAsState().value)
                }
        )
    },
    tabs = {
        LiveMainPvpTab(
            onClick = { selectTab(0) }
        )
        LiveMainLoadoutTab(
            onClick = { selectTab(1) }
        )
        LiveMainStoreTab(
            onClick = { selectTab(2) }
        )
    }
)