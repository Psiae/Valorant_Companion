package dev.flammky.valorantcompanion.live.pvp.party.presentation

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.compose.composeWithKey
import dev.flammky.valorantcompanion.base.util.mutableValueContainerOf
import dev.flammky.valorantcompanion.live.R
import kotlin.time.Duration

@Composable
fun InMatchmakingButton(
    modifier: Modifier,
    state: InMatchmakingButtonState
) = InMatchmakingButton(
    modifier = modifier,
    hasElapsedTime = state.hasElapsedTime,
    elapsedTime = state.elapsedTime,
    cancel = state.cancelMatchmaking
)

@Composable
fun InMatchmakingButton(
    modifier: Modifier,
    hasElapsedTime: Boolean,
    elapsedTime: Duration,
    cancel: () -> Unit
) {
    Box(
        modifier = modifier then remember(cancel) {
            Modifier
                .clip(RoundedCornerShape(50))
                .clickable(onClick = cancel)
                .background(Color(0xFFF4444C))
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .widthIn(min = 70.dp)
        },
    ) {
        val contentColor = Color.White
        if (hasElapsedTime) {
            composeWithKey(elapsedTime, contentColor) { p1, p2 ->
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "In Queue".uppercase(),
                        color = p2,
                        style = MaterialTheme.typography.labelLarge
                    )
                    BasicText(
                        text = remember(p1) {
                            val seconds = p1.inWholeSeconds.coerceAtLeast(0)
                            if (seconds > 3600) {
                                String.format(
                                    "%02d:%02d:%02d",
                                    seconds / 3600,
                                    seconds % 3600 / 60,
                                    seconds % 60
                                )
                            } else {
                                String.format(
                                    "%02d:%02d",
                                    seconds / 60,
                                    seconds % 60
                                )
                            }
                        },
                        style = composeWithKey(p2) { color ->
                            MaterialTheme.typography.labelLarge.copy(color = color)
                        }
                    )
                    composeWithKey(p2) { cc ->
                        Image(
                            modifier = Modifier
                                .size(18.dp),
                            painter = painterResource(id = R_ASSET_DRAWABLE.close_fill0_wght400_grad0_opsz48_f),
                            contentDescription = "cancel",
                            colorFilter = ColorFilter.tint(color = cc),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            }
        } else {
            composeWithKey(key1 = contentColor) { cc ->
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.CenterStart),
                    strokeWidth = 1.dp,
                    color = cc
                )
                Image(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.CenterEnd),
                    painter = painterResource(id = R_ASSET_DRAWABLE.close_fill0_wght400_grad0_opsz48_f),
                    contentDescription = "cancel",
                    colorFilter = ColorFilter.tint(color = cc),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}