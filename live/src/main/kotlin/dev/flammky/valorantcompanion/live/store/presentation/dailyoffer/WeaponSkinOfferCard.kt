package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
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
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.nearestBlackOrWhite
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.pvp.store.currency.*
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlin.math.roundToInt

// TODO: marquee display name
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeaponSkinOfferCard(
    modifier: Modifier,
    tier: WeaponSkinTier,
    displayImageKey: Any,
    displayImage: LocalImage<*>,
    displayName: String,
    cost: StoreCost,
    canOpenDetail: Boolean,
    openDetail: () -> Unit,
    shape: Shape
) = BoxWithConstraints(
    modifier
        .run {
            localMaterial3Surface(
                color = { color ->
                    Color(tier.highlightColor)
                        .copy(0.6f)
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

    Icon(
        modifier = Modifier
            .requiredSize(maxWidth * 0.65f)
            .offset {
                IntOffset(
                    x = (maxWidth.value * 0.5f * 0.5f).roundToInt().inv(),
                    y = (maxHeight.value * 0.25).roundToInt()
                )
            }
            .alpha(0.2f),
        painter = rememberWeaponSkinTierIconPainter(tier = tier),
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
                painter = rememberWeaponSkinTierIconPainter(tier),
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
                ,
                painter = painterResource(id = R_ASSET_DRAWABLE.right_arrow_100),
                contentDescription = null,
                tint = Material3Theme.surfaceContentColorAsState().value
            )
        }

        Row(horizontalArrangement = Arrangement.SpaceAround) {

            BasicText(
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                    )
                    .align(Alignment.Bottom),
                text = displayName,
                style = MaterialTheme3.typography.labelLarge.copy(
                    color = Material3Theme.blackOrWhiteContent(),
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                BasicText(
                    text = remember(cost.amount) {
                        cost.amount.toString().run {
                            if (length < 4) {
                                this
                            } else {
                                first()
                                    .plus(",")
                                    .plus(drop(1).chunked(3).joinToString(separator = ","))
                            }

                        }
                    },
                    style = MaterialTheme3.typography.labelLarge.copy(
                        color = Material3Theme.blackOrWhiteContent(),
                    )
                )

                if (!cost.currency.isOtherCurrency) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = rememberStoreCurrencyIconPainter(currency = cost.currency),
                        contentDescription = null,
                        tint = Material3Theme.surfaceContentColorAsState().value
                    )
                }
            }
        }
    }
}

// TODO: asset loader instead
@Composable
private fun rememberWeaponSkinTierIconPainter(
    tier: WeaponSkinTier
): Painter {
    val id = remember(tier) {
        when (tier) {
            WeaponSkinTier.SELECT -> R_ASSET_RAW.contenttier_select_displayicon
            WeaponSkinTier.DELUXE -> R_ASSET_RAW.contenttier_deluxe_displayicon
            WeaponSkinTier.PREMIUM -> R_ASSET_RAW.contenttier_premium_displayicon
            WeaponSkinTier.EXCLUSIVE -> R_ASSET_RAW.contenttier_exclusive_displayicon
            WeaponSkinTier.ULTRA -> R_ASSET_RAW.contenttier_ultra_displayicon
        }
    }
    return painterResource(id = id)
}

// TODO: asset loader instead
@Composable
private fun rememberStoreCurrencyIconPainter(
    currency: StoreCurrency
): Painter {
    val id = remember(currency) {
        when (currency) {
            KingdomCredit -> R_ASSET_RAW.currency_kc_displayicon
            RadianitePoint -> R_ASSET_RAW.currency_rp_displayicon
            ValorantPoint -> R_ASSET_RAW.currency_vp_displayicon
            is OtherStoreCurrency -> TODO()
        }
    }
    return painterResource(id = id)
}