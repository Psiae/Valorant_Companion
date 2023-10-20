package dev.flammky.valorantcompanion.live.store.presentation.agent

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.compose.tintElevatedSurface
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCurrency
import dev.flammky.valorantcompanion.pvp.store.currency.ValorantPoint
import kotlinx.collections.immutable.ImmutableList

@Composable
fun AgentDisplayCard(
    modifier: Modifier,
    contentPadding: PaddingValues,
    owned: Boolean,
    agentDisplayImageModifier: Modifier,
    agentDisplayImageKey: Any,
    agentDisplayImage: LocalImage<*>,
    agentRoleDisplayImageKey: Any,
    agentRoleDisplayImage: LocalImage<*>,
    agentName: String,
    canOpenDetail: Boolean,
    openDetail: () -> Unit
) {
    val ctx = LocalContext.current
    Box(
        modifier
            .composed {
                val isThemeDark = LocalIsThemeDark.current
                localMaterial3Surface(
                    color = { color ->
                        color.tintElevatedSurface(
                            tint = if (isThemeDark) {
                                Color.White
                            } else {
                                Color.Gray
                            },
                            elevation = 3.dp
                        )
                    },
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                    shape = RoundedCornerShape(8.dp)
                )
            }
            .clickable(enabled = canOpenDetail, onClick = openDetail)
            .padding(paddingValues = contentPadding)
    ) {
        Column {
            Row(modifier = Modifier) {
                Spacer(modifier = Modifier.weight(1f))
                if (owned) {
                    BasicText(
                        modifier = Modifier,
                        text = "OWNED",
                        style = MaterialTheme3.typography.labelMedium
                            .copy(
                                color = Color.Green,
                                shadow = remember { Shadow(offset = Offset(1f, 1f)) }
                            )
                    )
                } else {
                    BasicText(
                        modifier = Modifier,
                        text = "LOCKED",
                        style = MaterialTheme3.typography.labelMedium
                            .copy(
                                color = Color.Red,
                                shadow = remember { Shadow(offset = Offset(1f, 1f)) }
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(2)))
            Image(
                modifier = agentDisplayImageModifier,
                painter = rememberAsyncImagePainter(
                    model = remember(ctx, agentDisplayImageKey) {
                        ImageRequest.Builder(ctx)
                            .data(agentDisplayImage.value)
                            .build()
                    },
                ),
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
            Spacer(
                modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(2))
            )
            Row(modifier = Modifier) {
                BasicText(
                    modifier = Modifier,
                    text = agentName,
                    style = MaterialTheme3.typography.labelLarge
                        .copy(
                            color = Color.White,
                            shadow = remember { Shadow(offset = Offset(1f, 1f)) }
                        )
                )
                Spacer(
                    modifier = Modifier.weight(1f)
                )
                Image(
                    modifier = Modifier.size(18.dp),
                    painter = rememberAsyncImagePainter(
                        model = remember(ctx, agentRoleDisplayImageKey) {
                            ImageRequest.Builder(ctx)
                                .data(agentRoleDisplayImage.value)
                                .build()
                        },
                    ),
                    contentDescription = null
                )
            }
        }
    }
}