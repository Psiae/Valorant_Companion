package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.compose
import dev.flammky.valorantcompanion.base.compose.nearestBlackOrWhite
import dev.flammky.valorantcompanion.base.compose.rememberUpdatedStateWithKey
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.base.util.mutableValueContainerOf
import dev.flammky.valorantcompanion.pvp.store.currency.*
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlin.math.roundToInt

@Composable
fun WeaponSkinOfferCard(
    modifier: Modifier,
    state: WeaponSkinOfferCardState,
    canOpenDetail: Boolean,
    openDetail: () -> Unit,
    shape: Shape
) = WeaponSkinOfferCard(
    modifier = modifier,
    tier = state.tier,
    tierImageKey = state.tierImageKey,
    tierImage = state.tierImage,
    displayImageKey = state.displayImageKey,
    displayImage = state.displayImage,
    displayName = state.displayName,
    costText = state.costText,
    costImageKey = state.costImageKey,
    costImage = state.costImage,
    canOpenDetail = canOpenDetail,
    openDetail = openDetail,
    shape = shape
)


// TODO: refresh
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeaponSkinOfferCard(
    modifier: Modifier,
    tier: WeaponSkinTier,
    tierImageKey: Any,
    tierImage: LocalImage<*>,
    displayImageKey: Any,
    displayImage: LocalImage<*>,
    displayName: String,
    costText: String,
    costImageKey: Any,
    costImage: LocalImage<*>?,
    canOpenDetail: Boolean,
    openDetail: () -> Unit,
    shape: Shape
) = BoxWithConstraints(
    modifier
        .run {
            localMaterial3Surface(
                color = { color ->
                    Color(tier.highlightColor)
                        .compositeOver(color.nearestBlackOrWhite())
                },
                tonalElevation = 2.dp,
                tonalTint = Color(tier.highlightColor),
                shadowElevation = 2.dp,
                shape = shape
            )
        }
        .clickable(enabled = canOpenDetail, onClick = openDetail)
        .padding(8.dp)
) {

    val ctx = LocalContext.current

    Icon(
        modifier = Modifier
            .requiredSize(maxWidth * 0.65f)
            .offset {
                IntOffset(
                    x = (maxWidth.value * 0.5f * 0.5f)
                        .roundToInt()
                        .inv(),
                    y = (maxHeight.value * 0.25).roundToInt()
                )
            }
            .alpha(0.2f),
        painter = rememberAsyncImagePainter(
            model = remember(ctx, tierImageKey) {
                ImageRequest.Builder(ctx)
                    .data(tierImage.value)
                    .build()
            }
        ),
        contentDescription = null,
        tint = Color.Unspecified
    )
    
    Column {

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Icon(
                modifier = Modifier
                    .size(24.dp),
                painter = rememberAsyncImagePainter(
                    model = remember(ctx, tierImageKey) {
                        ImageRequest.Builder(ctx)
                            .data(tierImage.value)
                            .build()
                    }
                ),
                contentDescription = null,
                tint = Color.Unspecified
            )

            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                model = run {
                    val ctx = LocalContext.current
                    remember(ctx, displayImageKey) {
                        ImageRequest
                            .Builder(ctx)
                            .data(displayImage.value)
                            .build()
                    }
                },
                contentDescription = null
            )
            
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .interactiveUiElementAlphaEnforcement(true, canOpenDetail)
                ,
                painter = painterResource(id = R_ASSET_DRAWABLE.right_arrow_100),
                contentDescription = null,
                tint = Material3Theme.surfaceContentColorAsState().value
            )
        }

        Row(horizontalArrangement = Arrangement.SpaceAround) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Bottom)
            ) {
                BasicText(
                    modifier = Modifier
                        .basicMarquee(iterations = Int.MAX_VALUE),
                    text = displayName,
                    style = MaterialTheme3.typography.labelLarge.copy(
                        color = Material3Theme.blackOrWhiteContent(),
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                BasicText(
                    text = costText,
                    style = MaterialTheme3.typography.labelLarge.copy(
                        color = Material3Theme.blackOrWhiteContent(),
                    )
                )

                compose {
                    val imageModel = remember(ctx, costImageKey) {
                        if (costImage != null) {
                            ImageRequest.Builder(ctx)
                                .data(costImage.value)
                                .build()
                        } else {
                            null
                        }
                    }
                    if (imageModel != null) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = rememberAsyncImagePainter(model = imageModel),
                            contentDescription = null,
                            tint = Material3Theme.surfaceContentColorAsState().value
                        )
                    }
                }
            }
        }
    }
}