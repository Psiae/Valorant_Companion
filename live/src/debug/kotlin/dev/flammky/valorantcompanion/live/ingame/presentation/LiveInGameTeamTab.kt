package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.live.colorsystem.PVPColors

@Composable
@Preview
fun LiveInGameTabPreview() = DefaultMaterial3Theme(dark = true) {
    Box(
        modifier = Modifier.background(Material3Theme.surfaceColorAsState().value)
    ) {
        val allySelected = remember {
            mutableStateOf(true)
        }
        LiveInGameTeamTab(
            modifier = Modifier.align(Alignment.TopCenter),
            allySelected = allySelected.value,
            selectAlly = remember {
                { allySelected.value = true }
            },
            selectEnemy = remember {
                { allySelected.value = false }
            }
        )
    }
}

@Composable
fun LiveInGameTeamTab(
    modifier: Modifier,
    allySelected: Boolean,
    selectAlly: () -> Unit,
    selectEnemy: () -> Unit
    // scroll connection ?
) = TabRow(
    modifier = modifier,
    selectedTabIndex = if (allySelected) 0 else 1,
    backgroundColor = Material3Theme.surfaceColorAsState().value,
    contentColor = Material3Theme.surfaceContentColorAsState().value,
    indicator = @Composable { tabPositions ->
        Box(
            Modifier
                .composed {
                    val tabPosition =
                        tabPositions[if (allySelected) 0 else 1]
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
                        .background(
                            // maybe don't change color until the bar is in the range ?
                            color = remember(allySelected) {
                                if (allySelected) Color(PVPColors.ALLY_ARGB)
                                else Color(PVPColors.ENEMY_ARGB)
                            }
                        )
                }
        )
    },
    tabs = {
        AllyTeamTabs(
            onClick = selectAlly
        )
        EnemyTeamTabs(
            onClick = selectEnemy
        )
    }
)

@Composable
private fun AllyTeamTabs(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(15.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "ALLY",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
            color = if (LocalIsThemeDark.current) Color.White else Color.Black
        )
    }
}

@Composable
private fun EnemyTeamTabs(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(15.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "ENEMY",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
            color = if (LocalIsThemeDark.current) Color.White else Color.Black
        )
    }
}