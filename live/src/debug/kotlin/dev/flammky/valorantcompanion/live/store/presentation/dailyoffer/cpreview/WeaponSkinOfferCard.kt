package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Surface
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.WeaponSkinOfferCard
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.ValorantPoint
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier

@Composable
@Preview
private fun WeaponSkinOfferCardPreview() {

    DefaultMaterial3Theme(dark = isSystemInDarkTheme()) {

        Column(
            Modifier
                .fillMaxSize()
                .localMaterial3Surface()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            WeaponSkinOfferCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                tier = WeaponSkinTier.EXCLUSIVE,
                displayImageKey = Any(),
                displayImage = LocalImage.Resource(R_ASSET_RAW.debug_exclusive_neofrontier_melee_displayicon),
                displayName = "Neo Frontier Axe",
                cost = StoreCost(ValorantPoint, 4_350),
                canOpenDetail = true,
                openDetail = {},
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            WeaponSkinOfferCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                tier = WeaponSkinTier.ULTRA,
                displayImageKey = Any(),
                displayImage = LocalImage.Resource(R_ASSET_RAW.debug_ultra_spectrum_phantom_displayicon),
                displayName = "Spectrum Phantom",
                cost = StoreCost(ValorantPoint, 2_675),
                canOpenDetail = true,
                openDetail = {},
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            WeaponSkinOfferCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                tier = WeaponSkinTier.PREMIUM,
                displayImageKey = Any(),
                displayImage = LocalImage.Resource(R_ASSET_RAW.debug_oni_shorty_displayicon),
                displayName = "Oni Shorty",
                cost = StoreCost(ValorantPoint, 1_775),
                canOpenDetail = true,
                openDetail = {},
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            WeaponSkinOfferCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                tier = WeaponSkinTier.DELUXE,
                displayImageKey = Any(),
                displayImage = LocalImage.Resource(R_ASSET_RAW.debug_deluxe_sakura_stringer_displayicon),
                displayName = "Sakura Stringer",
                cost = StoreCost(ValorantPoint, 1_275),
                canOpenDetail = true,
                openDetail = {},
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            WeaponSkinOfferCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                tier = WeaponSkinTier.SELECT,
                displayImageKey = Any(),
                displayImage = LocalImage.Resource(R_ASSET_RAW.debug_select_endeavour_bulldog_displayicon),
                displayName = "Endeavour Bulldog",
                cost = StoreCost(ValorantPoint, 875),
                canOpenDetail = true,
                openDetail = {},
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}