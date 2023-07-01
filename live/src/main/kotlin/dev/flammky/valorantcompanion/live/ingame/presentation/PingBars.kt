package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.live.pingStrengthInRangeOf4

@Composable
internal fun DrawLiveInGameTopBar4PingBar(
    modifier: Modifier,
    pingMs: Int
) {
    val strength = pingStrengthInRangeOf4(pingMs)
    check(strength in 1..4)
    val color = when (strength) {
        1 -> Color.Red
        2 -> Color.Yellow
        3, 4 -> Color.Green
        else -> error("Unguarded condition")
    }
    BoxWithConstraints(
        modifier = modifier.size(24.dp)
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(4) { i ->
                val n = i + 1
                val height = (maxHeight.value * (n.toFloat() / 4)).dp
                val width = maxHeight / 4
                Box(
                    modifier = Modifier
                        .height(height)
                        .width(width)
                        .background(color.takeIf { strength >= n } ?: Color.Gray)
                        .shadow(2.dp)
                )
            }
        }
    }
}