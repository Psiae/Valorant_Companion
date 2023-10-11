package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.NoOpPainter
import dev.flammky.valorantcompanion.base.compose.rememberUpdatedStateWithKey
import dev.flammky.valorantcompanion.base.compose.tintElevatedSurface
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.pvp.store.currency.*
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlinx.collections.immutable.ImmutableSet
import kotlin.time.Duration

@Composable
fun FeaturedBundleDisplay(
    modifier: Modifier,
    state: FeaturedBundleDisplayState,
    shape: Shape,
) = FeaturedBundleDisplay(
    modifier = modifier,
    bundleName = state.displayName,
    durationLeft = state.durationLeft,
    tiersKey = state.tiersKey,
    tiers = state.tiers,
    cost = state.cost,
    imageKey = state.imageKey,
    image = state.image,
    shape = shape
)


// TODO: panel
@Composable
fun FeaturedBundleDisplay(
    modifier: Modifier,
    bundleName: String,
    durationLeft: Duration,
    tiersKey: Any,
    tiers: ImmutableSet<WeaponSkinTier>,
    cost: StoreCost,
    imageKey: Any,
    image: LocalImage<*>,
    shape: Shape,
) {
    Column(
        modifier
            .localMaterial3Surface(
                shape = shape,
                shadowElevation = 3.dp
            )
    ) {

        Column(
            modifier = Modifier
                .background(
                    Material3Theme.foldLightOrDarkTheme(
                        light = {
                            Material3Theme.surfaceColorAsState().value.let { sfc ->
                                remember(sfc) { sfc.tintElevatedSurface(Color.DarkGray, 3.dp) }
                            }
                        },
                        dark = {
                            Material3Theme.surfaceColorAsState().value.let { sfc ->
                                remember(sfc) { sfc.tintElevatedSurface(Color.LightGray, 3.dp) }
                            }
                        }
                    )
                )
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {

            Row {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                        .align(Alignment.CenterVertically),
                    text = bundleName,
                    style = MaterialTheme3.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Material3Theme.blackOrWhiteContent()
                    )
                )

                BasicText(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    text = remember(durationLeft) {
                        durationLeft.toComponents { days, hours, minutes, seconds, nanoseconds ->
                            "${days}D $hours:$minutes:$seconds"
                        }
                    },
                    style = MaterialTheme3.typography.labelMedium.copy(
                        fontWeight = FontWeight.Light,
                        color = Material3Theme.blackOrWhiteContent()
                    )
                )
                Spacer(Modifier.width(Material3Theme.dpPaddingIncrementsOf(1)))
                Icon(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.CenterVertically),
                    painter = painterResource(id = R_ASSET_DRAWABLE.time_machine_ios_16_glyph_100px),
                    contentDescription = null,
                    tint = Material3Theme.blackOrWhiteContent()
                )
            }

            Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(2)))

            Row {

                Row(
                    modifier = Modifier
                        .weight(1f)
                ) {

                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        painter = rememberStoreCurrencyIconPainter(currency = cost.currency),
                        contentDescription = null,
                        tint = Material3Theme.surfaceContentColorAsState().value
                    )

                    Spacer(modifier = Modifier.width(Material3Theme.dpPaddingIncrementsOf(1)))

                    BasicText(
                        modifier = Modifier
                            .align(Alignment.CenterVertically),
                        text = cost.amount.toString(),
                        style = MaterialTheme3
                            .typography
                            .labelLarge
                            .copy(
                                color = Material3Theme.surfaceContentColorAsState().value,

                            )
                    )
                }

                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .interactiveUiElementAlphaEnforcement(true, true),
                    painter = painterResource(id = R_ASSET_DRAWABLE.right_arrow_100),
                    contentDescription = null,
                    tint = Material3Theme.surfaceContentColorAsState().value
                )
            }
        }

        Box {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = run {
                    val ctx = LocalContext.current
                    remember (ctx, imageKey) {
                        ImageRequest.Builder(ctx)
                            .data(image.value)
                            .build()
                    }
                },
                contentScale = ContentScale.FillBounds,
                contentDescription = "Featured Bundle"
            )

            run tierIcon@ {
                val size = 24.dp
                val shadowWidth = 0.6.dp

                val tierSet = rememberUpdatedStateWithKey(key = tiersKey, value = tiers).value

                Row {
                    tierSet.forEachIndexed { i, tier ->
                        Box {
                            // temporary shadow impl
                            Icon(
                                modifier = Modifier
                                    .padding(Material3Theme.dpPaddingIncrementsOf(2))
                                    .size(size + shadowWidth),
                                painter = rememberWeaponSkinTierIconPainter(tier = tier),
                                contentDescription = null,
                                tint = Color.Black
                            )

                            Icon(
                                modifier = Modifier
                                    .padding(Material3Theme.dpPaddingIncrementsOf(2))
                                    .size(size),
                                painter = rememberWeaponSkinTierIconPainter(tier = tier),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                        if (i < tierSet.size -1) {
                            Spacer(modifier = Modifier.width(Material3Theme.dpPaddingIncrementsOf(1)))
                        }
                    }
                }
            }
        }
    }
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
            is OtherStoreCurrency -> 0
        }
    }
    if (id == 0) return NoOpPainter
    return painterResource(id = id)
}

@Composable
private fun rememberWeaponSkinTierIconPainter(
    tier: WeaponSkinTier
): Painter {
    val id = rememberWeaponSkinTierIconResId(tier = tier)
    if (id == 0) return NoOpPainter
    return painterResource(id = id)
}

@Composable
private fun rememberWeaponSkinTierIconResId(tier: WeaponSkinTier): Int {
    return remember(tier) {
        when (tier) {
            WeaponSkinTier.SELECT -> R_ASSET_RAW.contenttier_select_displayicon
            WeaponSkinTier.DELUXE -> R_ASSET_RAW.contenttier_deluxe_displayicon
            WeaponSkinTier.PREMIUM -> R_ASSET_RAW.contenttier_premium_displayicon
            WeaponSkinTier.EXCLUSIVE -> R_ASSET_RAW.contenttier_exclusive_displayicon
            WeaponSkinTier.ULTRA -> R_ASSET_RAW.contenttier_ultra_displayicon
            WeaponSkinTier.OTHER -> 0
            WeaponSkinTier.UNSET -> 0
        }
    }
}