package dev.flammky.valorantcompanion.live.pvp.pregame.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceColorAsState
import dev.flammky.valorantcompanion.base.theme.material3.surfaceVariantColorAsState
import dev.flammky.valorantcompanion.live.pingStrengthInRangeOf4
import kotlin.time.Duration

@Composable
internal fun LivePreGameTopBar(
    modifier: Modifier,
    mapName: String,
    gameTypeName: String,
    gamePodName: String,
    gamePodPing: Int,
    countDown: Duration?
) {
    // TODO
    val mapModel = remember {
        mutableStateOf<ImageRequest?>(null)
    }
    Surface(
        modifier = modifier,
        color = Material3Theme.surfaceColorAsState().value
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    modifier = Modifier.matchParentSize(),
                    model = mapModel.value,
                    contentDescription = "Map $mapName",
                    contentScale = ContentScale.Crop,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f, false)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Material3Theme.surfaceVariantColorAsState().value.copy(alpha = 0.97f))
                            .padding(12.dp)
                    ) {
                        val textColor = if (LocalIsThemeDark.current) Color.White else Color.Black
                        Text(
                            text = "Map - $mapName".uppercase(),
                            color = textColor,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = gameTypeName.uppercase(),
                            color = textColor,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row {
                            Text(
                                modifier = Modifier.weight(1f, false),
                                text = gamePodName.ifBlank { "???" },
                                color = textColor,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (gamePodPing > -1) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    modifier = Modifier.weight(1f, false),
                                    text = "(${gamePodPing}ms)",
                                    color = textColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(1.dp))
                                DrawLiveInGameTopBar4PingBar(modifier = Modifier
                                    .height(8.dp)
                                    .align(Alignment.CenterVertically), pingMs = 25)
                            }
                        }
                    }
                    run countdown@ {
                        val stepSec = countDown?.inWholeSeconds ?: -1
                        Log.d("LivePreGame.kt", "TopBarInfo_countDown@: stepSec=$stepSec")
                        if (stepSec < 0 || stepSec == Duration.INFINITE.inWholeSeconds) return@countdown
                        Spacer(modifier = Modifier.width(12.dp))
                        BoxWithConstraints(
                            modifier = Modifier
                                .size(68.dp)
                                .align(Alignment.CenterVertically)
                                .clip(CircleShape)
                                .background(
                                    Material3Theme.surfaceVariantColorAsState().value.copy(
                                        alpha = 0.97f
                                    )
                                )
                        ) {

                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.Center),
                                progress = stepSec.toFloat() / 85,
                                strokeWidth = 2.dp,
                                color = if (stepSec <= 10f) Color.Red else Color.Green
                            )
                            val text = stepSec.toInt().toString()
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(6.dp),
                                text = text,
                                textAlign = TextAlign.Center,
                                color = if (LocalIsThemeDark.current) Color.White else Color.Black,
                                style = MaterialTheme.typography.labelLarge
                                    .copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize =  when (text.length) {
                                            1 -> 43.sp
                                            else -> ((68.dp - 6.dp - 2.dp) / text.length).value.sp
                                        }.also {
                                            Log.d(
                                                "LivePreGame.kt",
                                                "TopBarInfo_countDown@: fontSize=$it")
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawLiveInGameTopBar4PingBar(
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