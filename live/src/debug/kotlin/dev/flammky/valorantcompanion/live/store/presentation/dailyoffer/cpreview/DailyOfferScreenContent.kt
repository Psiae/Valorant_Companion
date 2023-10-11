package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview

import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.compose
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.koin.compose.KoinDependencyInjector
import dev.flammky.valorantcompanion.base.referentialEqualityFun
import dev.flammky.valorantcompanion.base.rememberWithEquality
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.DailyOfferScreenContent
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.FeaturedBundleDisplayPager
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.rememberDailyOfferScreenPresenter
import dev.flammky.valorantcompanion.pvp.store.*
import dev.flammky.valorantcompanion.pvp.store.currency.*
import dev.flammky.valorantcompanion.pvp.store.debug.StubValorantStoreService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Preview
@Composable
private fun DailyOfferScreenContentPreview() {

    DefaultMaterial3Theme(dark = isSystemInDarkTheme()) {

        val provisioned = remember {
            mutableStateOf(false)
        }

        LaunchedEffect(key1 = Unit, block = {
            provisioned.value = false
            stopKoin()
            startKoin {
                modules(
                    module {
                        single<ValorantAssetsService> {
                            DebugValorantAssetService()
                        }
                        single<ValorantStoreService> {
                            StubValorantStoreService(
                                storeFrontProvider = { id ->
                                    if (id == "dokka") {
                                        return@StubValorantStoreService StoreFrontData(
                                            featuredBundleStore = FeaturedBundleStore(
                                                open = true,
                                                offer = Result.success(
                                                    FeaturedBundleStore.Offer(
                                                        bundle = FeaturedBundleStore.Bundle(
                                                            id = "neofrontier",
                                                            dataAssetID = "",
                                                            currencyID = StoreCurrency.ValorantPoint.uuid,
                                                            itemOffers = persistentListOf(),
                                                            itemDiscountedOffers = persistentListOf(),
                                                            totalBaseCost = StoreCost(StoreCurrency.ValorantPoint, 6100),
                                                            totalDiscountedCost = StoreCost(StoreCurrency.ValorantPoint, 6100),
                                                            totalDiscountPercent = 0f,
                                                            durationRemaining = 12.days + 10.hours + 35.minutes + 20.seconds,
                                                            wholesaleOnly = false
                                                        ),
                                                        bundles = persistentListOf(
                                                            FeaturedBundleStore.Bundle(
                                                                id = "neofrontier",
                                                                dataAssetID = "",
                                                                currencyID = StoreCurrency.ValorantPoint.uuid,
                                                                itemOffers = persistentListOf(),
                                                                itemDiscountedOffers = persistentListOf(),
                                                                totalBaseCost = StoreCost(StoreCurrency.ValorantPoint, 6100),
                                                                totalDiscountedCost = StoreCost(StoreCurrency.ValorantPoint, 6100),
                                                                totalDiscountPercent = 0f,
                                                                durationRemaining = 12.days + 10.hours + 35.minutes + 20.seconds,
                                                                wholesaleOnly = false
                                                            ),
                                                            FeaturedBundleStore.Bundle(
                                                                id = "neofrontier",
                                                                dataAssetID = "",
                                                                currencyID = StoreCurrency.ValorantPoint.uuid,
                                                                itemOffers = persistentListOf(),
                                                                itemDiscountedOffers = persistentListOf(),
                                                                totalBaseCost = StoreCost(StoreCurrency.ValorantPoint, 6100),
                                                                totalDiscountedCost = StoreCost(StoreCurrency.ValorantPoint, 6100),
                                                                totalDiscountPercent = 0f,
                                                                durationRemaining = 12.days + 10.hours + 35.minutes + 20.seconds,
                                                                wholesaleOnly = false
                                                            )
                                                        ),
                                                        bundleRemainingDuration = 12.days + 10.hours + 35.minutes + 20.seconds
                                                    )
                                                )
                                            ),
                                            skinsPanel = SkinsPanelStore(
                                                open = true,
                                                offer = Result.success(
                                                    SkinsPanelStore.Offer(
                                                        offeredItemIds = persistentListOf(
                                                            "dbg_wpn_skn_exc_neofrontier_melee",
                                                            "dbg_wpn_skn_ult_spectrum_phantom",
                                                            "dbg_wpn_skn_prmm_oni_sorty",
                                                            "dbg_wpn_skn_dlx_sakura_stinger",
                                                            "dbg_wpn_skn_slct_endeavour_bulldog"
                                                        ),
                                                        itemOffers = persistentMapOf<String, SkinsPanelStore.ItemOffer>()
                                                            .builder()
                                                            .apply {
                                                                put(
                                                                    "dbg_offr_wpn_skn_exc_neofrontier_melee",
                                                                    SkinsPanelStore.ItemOffer(
                                                                        "1",
                                                                        false,
                                                                        ISO8601.fromEpochMilli(ISO8601.START_OF_TIME_EPOCH_MILLIS),
                                                                        StoreCost(
                                                                            ValorantPoint,
                                                                            4350
                                                                        ),
                                                                        reward = SkinsPanelStore.Reward(
                                                                            ItemType.Skin,
                                                                            itemID = "dbg_wpn_skn_exc_neofrontier_melee",
                                                                            quantity = 1
                                                                        )
                                                                    )
                                                                )
                                                                put(
                                                                    "dbg_offr_wpn_skn_ult_spectrum_phantom",
                                                                    SkinsPanelStore.ItemOffer(
                                                                        "2",
                                                                        isDirectPurchase = false,
                                                                        ISO8601.fromEpochMilli(ISO8601.START_OF_TIME_EPOCH_MILLIS),
                                                                        StoreCost(
                                                                            ValorantPoint,
                                                                            2675
                                                                        ),
                                                                        reward = SkinsPanelStore.Reward(
                                                                            ItemType.Skin,
                                                                            itemID = "dbg_wpn_skn_ult_spectrum_phantom",
                                                                            quantity = 1
                                                                        )
                                                                    )
                                                                )
                                                                put(
                                                                    "dbg_offr_wpn_skn_prmm_oni_shorty",
                                                                    SkinsPanelStore.ItemOffer(
                                                                        "3",
                                                                        isDirectPurchase = false,
                                                                        ISO8601.fromEpochMilli(ISO8601.START_OF_TIME_EPOCH_MILLIS),
                                                                        StoreCost(
                                                                            ValorantPoint,
                                                                            1775
                                                                        ),
                                                                        reward = SkinsPanelStore.Reward(
                                                                            ItemType.Skin,
                                                                            itemID = "dbg_wpn_skn_prmm_oni_shorty",
                                                                            quantity = 1
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            .build(),
                                                        remainingDuration = 15.hours + 10.minutes + 20.seconds
                                                    )
                                                )
                                            ),
                                            upgradeCurrencyStore = UpgradeCurrencyStore.UNSET,
                                            accessoryStore = AccessoryStore(
                                                open = true,
                                                offer = Result.success(
                                                    AccessoryStore.Offer(
                                                        storeFrontId = "dbg_offr_acc",
                                                        persistentListOf<AccessoryStore.ItemOffer>()
                                                            .builder()
                                                            .apply {
                                                                add(
                                                                    AccessoryStore.ItemOffer(
                                                                        id = "dbg_offr_acc_spray_0x3c_3",
                                                                        isDirectPurchase = true,
                                                                        startDate = ISO8601.fromEpochMilli(ISO8601.START_OF_TIME_EPOCH_MILLIS),
                                                                        cost = StoreCost(
                                                                            currency = ValorantPoint,
                                                                            amount = 3000
                                                                        ),
                                                                        reward = AccessoryStore.Reward(
                                                                            itemType = ItemType.Spray,
                                                                            itemID  = "dbg_acc_spray_0x3d_3",
                                                                            quantity = 1
                                                                        ),
                                                                        contractID = "1"
                                                                    )
                                                                )
                                                                add(
                                                                    AccessoryStore.ItemOffer(
                                                                        id = "dbg_offr_acc_spray_0x3c_3_v2",
                                                                        isDirectPurchase = true,
                                                                        startDate = ISO8601.fromEpochMilli(ISO8601.START_OF_TIME_EPOCH_MILLIS),
                                                                        cost = StoreCost(
                                                                            currency = RadianitePoint,
                                                                            amount = 3000
                                                                        ),
                                                                        reward = AccessoryStore.Reward(
                                                                            itemType = ItemType.Spray,
                                                                            itemID  = "dbg_acc_spray_0x3d_3",
                                                                            quantity = 1
                                                                        ),
                                                                        contractID = "1_v2"
                                                                    )
                                                                )
                                                                add(
                                                                    AccessoryStore.ItemOffer(
                                                                        id = "dbg_offr_acc_playercard_yearone",
                                                                        isDirectPurchase = true,
                                                                        startDate = ISO8601.fromEpochMilli(ISO8601.START_OF_TIME_EPOCH_MILLIS),
                                                                        cost = StoreCost(
                                                                            currency = KingdomCredit,
                                                                            amount = 3000
                                                                        ),
                                                                        reward = AccessoryStore.Reward(
                                                                            itemType = ItemType.PlayerCard,
                                                                            itemID  = "dbg_acc_playercard_yearone",
                                                                            quantity = 1
                                                                        ),
                                                                        contractID = "2"
                                                                    )
                                                                )
                                                                add(
                                                                    AccessoryStore.ItemOffer(
                                                                        id = "dbg_offr_acc_playercard_yearone_v2",
                                                                        isDirectPurchase = true,
                                                                        startDate = ISO8601.fromEpochMilli(ISO8601.START_OF_TIME_EPOCH_MILLIS),
                                                                        cost = StoreCost(
                                                                            currency = KingdomCredit,
                                                                            amount = 3000
                                                                        ),
                                                                        reward = AccessoryStore.Reward(
                                                                            itemType = ItemType.PlayerCard,
                                                                            itemID  = "dbg_acc_playercard_yearone",
                                                                            quantity = 1
                                                                        ),
                                                                        contractID = "2_v2"
                                                                    )
                                                                )
                                                            }
                                                            .associateByTo(persistentMapOf<String, AccessoryStore.ItemOffer>().builder()) { it.id }
                                                            .build(),
                                                        remainingDuration = 4.days + 6.hours + 20.minutes
                                                    )
                                                )
                                            ),
                                            bonusStore = BonusStore.UNSET
                                        )
                                    }
                                    null
                                },
                                bundleDataProvider = { id ->
                                    if (id == "neofrontier") {
                                        return@StubValorantStoreService FeaturedBundleDisplayData(
                                            uuid = "neofrontier",
                                            displayName = "NEO FRONTIER",
                                            displayNameSubText = null,
                                            description = "",
                                            extraDescription = null,
                                            useAdditionalContext = false
                                        )
                                    }
                                    null
                                }
                            )
                        }
                    }
                )
            }
            provisioned.value = true
        })

        CompositionLocalProvider(LocalDependencyInjector provides remember { KoinDependencyInjector(
            GlobalContext
        ) }) {
            if (provisioned.value) {
                val dailyOfferState = rememberDailyOfferScreenPresenter(
                    di = LocalDependencyInjector.current
                ).present(user = "dokka", isVisibleToUser = true)

                DailyOfferScreenContent(dailyOfferState = dailyOfferState)
            }
        }
    }
}