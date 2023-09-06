package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.compose
import dev.flammky.valorantcompanion.base.compose.rememberUpdatedStateWithKey
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.WeaponSkinOfferCard
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.ValorantPoint
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.Objects
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
@Preview
private fun WeaponSkinOfferPanelPreview() {

    DefaultMaterial3Theme(dark = isSystemInDarkTheme()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .localMaterial3Surface()
                .verticalScroll(rememberScrollState())
                .padding(
                    vertical = Material3Theme.dpPaddingIncrementsOf(2),
                    horizontal = Material3Theme.dpPaddingIncrementsOf(4)
                )
        ) {

            WeaponSkinOfferPanel(
                modifier = Modifier,
                durationLeft = 15.hours + 10.minutes + 20.seconds,
                itemKey = Any(),
                items = persistentListOf(
                    WeaponSkinOfferPanelItem(
                        uuid = "dbg_wpn_skn_exc_neofrontier_melee",
                        tier = WeaponSkinTier.EXCLUSIVE,
                        cost = StoreCost(ValorantPoint, 4_350),
                        displayName = "Neo Frontier Axe",
                        displayImageKey = Any(),
                        displayImage = LocalImage.Resource(R_ASSET_RAW.debug_exclusive_neofrontier_melee_displayicon)
                    ),
                    WeaponSkinOfferPanelItem(
                        uuid = "dbg_wpn_skn_ult_spectrum_phantom",
                        tier = WeaponSkinTier.ULTRA,
                        cost = StoreCost(ValorantPoint, 2_675),
                        displayName = "Spectrum Phantom",
                        displayImageKey = Any(),
                        displayImage = LocalImage.Resource(R_ASSET_RAW.debug_ultra_spectrum_phantom_displayicon)
                    ),
                    WeaponSkinOfferPanelItem(
                        uuid = "dbg_wpn_skn_prmm_oni_shorty",
                        tier = WeaponSkinTier.PREMIUM,
                        cost = StoreCost(ValorantPoint, 1_775),
                        displayName = "Oni Shorty",
                        displayImageKey = Any(),
                        displayImage = LocalImage.Resource(R_ASSET_RAW.debug_premium_oni_shorty_displayicon)
                    ),
                    WeaponSkinOfferPanelItem(
                        uuid = "dbg_wpn_skn_dlx_sakura_stinger",
                        tier = WeaponSkinTier.DELUXE,
                        cost = StoreCost(ValorantPoint, 1_275),
                        displayName = "Sakura Stinger",
                        displayImageKey = Any(),
                        displayImage = LocalImage.Resource(R_ASSET_RAW.debug_deluxe_sakura_stringer_displayicon)
                    ),
                    WeaponSkinOfferPanelItem(
                        uuid = "dbg_wpn_skn_slct_endeavour_bulldog",
                        tier = WeaponSkinTier.SELECT,
                        cost = StoreCost(ValorantPoint, 875),
                        displayName = "Endeavour Bulldog",
                        displayImageKey = Any(),
                        displayImage = LocalImage.Resource(R_ASSET_RAW.debug_select_endeavour_bulldog_displayicon)
                    )
                ),
                canOpenDetail = { true },
                openDetail = {  }
            )
        }
    }
}

@Immutable
class WeaponSkinOfferPanelItem(
    val uuid: String,
    val tier: WeaponSkinTier,
    val cost: StoreCost,
    val displayName: String,
    val displayImageKey: Any,
    val displayImage: LocalImage<*>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WeaponSkinOfferPanelItem) return false

        return uuid == other.uuid &&
                tier == other.tier &&
                cost == other.cost &&
                displayName == other.displayName &&
                displayImageKey == other.displayImageKey
    }

    override fun hashCode(): Int {
        return Objects.hash(uuid, tier, cost, displayName, displayImageKey)
    }
}

@Composable
fun WeaponSkinOfferPanel(
    modifier: Modifier,
    durationLeft: Duration,
    itemKey: Any,
    items: ImmutableList<WeaponSkinOfferPanelItem>,
    canOpenDetail: (WeaponSkinOfferPanelItem) -> Boolean,
    openDetail: (WeaponSkinOfferPanelItem) -> Unit
) {

    Column(modifier = modifier) {

        DailyOfferHeader(
            modifier = Modifier,
            durationLeft = durationLeft
        )

        Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(5)))

        compose(rememberUpdatedStateWithKey(key = itemKey, value = items)) { itemsState ->

            itemsState.value.forEach { item ->

                WeaponSkinOfferCard(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(),
                    tier = item.tier,
                    displayImageKey = item.displayImageKey,
                    displayImage = item.displayImage,
                    displayName = item.displayName,
                    cost = item.cost,
                    canOpenDetail = canOpenDetail(item),
                    openDetail = { openDetail(item) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(3)))
            }
        }
    }
}

@Composable
private fun DailyOfferHeader(
    modifier: Modifier,
    durationLeft: Duration
) {
    Row(modifier) {

        BasicText(
            modifier = Modifier.weight(1f),
            text = "Daily Offer",
            style = MaterialTheme3.typography.titleMedium.copy(
                color = Material3Theme.surfaceContentColorAsState().value
            )
        )

        Row(modifier = Modifier.align(Alignment.CenterVertically)) {

            BasicText(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = remember(durationLeft) {
                    durationLeft.toComponents { _, hours, minutes, seconds, _ ->
                        "${hours}h ${minutes}:${seconds}"
                    }
                },
                style = MaterialTheme3.typography.labelLarge.copy(
                    color = Material3Theme.surfaceContentColorAsState().value
                )
            )

            Spacer(modifier = Modifier.width(Material3Theme.dpPaddingIncrementsOf(2)))

            Icon(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R_ASSET_DRAWABLE.time_machine_ios_16_glyph_100px),
                contentDescription = null,
                tint = Material3Theme.surfaceContentColorAsState().value
            )
        }
    }
}