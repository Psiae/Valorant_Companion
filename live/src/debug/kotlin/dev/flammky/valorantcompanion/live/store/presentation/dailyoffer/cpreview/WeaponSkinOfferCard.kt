package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview

import android.webkit.WebView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Surface
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.WeaponSkinOfferCard
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.WeaponSkinOfferCardState
import dev.flammky.valorantcompanion.pvp.di.KoinPvpModule
import dev.flammky.valorantcompanion.pvp.store.FeaturedBundleDisplayData
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.ValorantPoint
import dev.flammky.valorantcompanion.pvp.store.debug.StubValorantStoreService
import dev.flammky.valorantcompanion.pvp.store.weapon.skin.WeaponSkinTier
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

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
                state = WeaponSkinOfferCardState(
                    tier = WeaponSkinTier.EXCLUSIVE,
                    tierImageKey = Any(),
                    tierImage = LocalImage.Resource(R_ASSET_RAW.contenttier_exclusive_displayicon),
                    displayImageKey = Any(),
                    displayImage = LocalImage.Resource(R_ASSET_RAW.debug_wpn_skn_exclusive_neofrontier_melee_displayicon),
                    displayName = "Neo Frontier Axe",
                    costText = "4,350",
                    costImageKey = Any(),
                    costImage = LocalImage.Resource(R_ASSET_RAW.currency_vp_displayicon),
                    showLoading = false,
                    requireRefresh = false,
                    refresh = {}
                ),
                canOpenDetail = true,
                openDetail = {},
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}