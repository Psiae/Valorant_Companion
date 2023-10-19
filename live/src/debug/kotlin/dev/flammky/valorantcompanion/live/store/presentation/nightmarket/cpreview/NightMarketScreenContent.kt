package dev.flammky.valorantcompanion.live.store.presentation.nightmarket.cpreview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.koin.compose.KoinDependencyInjector
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Surface
import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.live.store.presentation.nightmarket.NightMarketScreenContent
import dev.flammky.valorantcompanion.live.store.presentation.nightmarket.NightMarketScreenState
import dev.flammky.valorantcompanion.pvp.store.BonusStore
import dev.flammky.valorantcompanion.pvp.store.ItemType
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.ValorantPoint
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import kotlin.math.ceil
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Preview
@Composable
fun NightMarketScreenContentPreview() {
    val provisionedState = remember {
        mutableStateOf(false)
    }
    LaunchedEffect(
        key1 = Unit,
        block = {
            provisionedState.value = false
            GlobalContext.stopKoin()
            GlobalContext.startKoin {
                modules(
                    module {
                        single<ValorantAssetsService> {
                            DebugValorantAssetService()
                        }
                    }
                )
            }
            provisionedState.value = true
        }
    )
    if (!provisionedState.value) {
        return
    }
    CompositionLocalProvider(
        LocalDependencyInjector provides remember { KoinDependencyInjector(GlobalContext) }
    ) {
        DefaultMaterial3Theme(dark = true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .localMaterial3Surface()
            ) {
                NightMarketScreenContent(
                    modifier = Modifier,
                    state = remember {
                        NightMarketScreenState(
                            bonusStore = BonusStore(
                                open = true,
                                offer = Result.success(
                                    value = BonusStore.Offer(
                                        offers = persistentListOf<BonusStore.ItemOffer>().builder()
                                            .apply {
                                                add(
                                                    BonusStore.ItemOffer(
                                                        bonusOfferID = "1",
                                                        offerID = "1",
                                                        isDirectPurchase = true,
                                                        startDate = ISO8601.fromEpochMilli(ISO8601.START_OF_TIME_EPOCH_MILLIS),
                                                        cost = StoreCost(
                                                            currency = ValorantPoint,
                                                            amount = 4350
                                                        ),
                                                        discountPercent = 35,
                                                        discountedCost = StoreCost(
                                                            currency = ValorantPoint,
                                                            amount = ceil(((100 - 35) / 100f * 4350f)).toLong()
                                                        ),
                                                        isSeen = true,
                                                        rewards = persistentMapOf<String, BonusStore.ItemOfferReward>()
                                                            .builder()
                                                            .apply {
                                                                val itemID = "dbg_wpn_skn_exc_neofrontier_melee"
                                                                put(
                                                                    itemID,
                                                                    BonusStore.ItemOfferReward(
                                                                        itemType = ItemType.WeaponSkin,
                                                                        itemID = itemID,
                                                                        quantity = 1
                                                                    )
                                                                )
                                                            }
                                                            .build()
                                                    )
                                                )
                                                add(
                                                    BonusStore.ItemOffer(
                                                        bonusOfferID = "1",
                                                        offerID = "2",
                                                        isDirectPurchase = true,
                                                        startDate = ISO8601.fromEpochMilli(ISO8601.START_OF_TIME_EPOCH_MILLIS),
                                                        cost = StoreCost(
                                                            ValorantPoint,
                                                            2675
                                                        ),
                                                        discountPercent = 25,
                                                        discountedCost = StoreCost(
                                                            currency = ValorantPoint,
                                                            amount = ceil(((100 - 25) / 100f * 2675)).toLong()
                                                        ),
                                                        rewards = persistentMapOf<String, BonusStore.ItemOfferReward>()
                                                            .builder()
                                                            .apply {
                                                                val itemID = "dbg_wpn_skn_ult_spectrum_phantom"
                                                                put(
                                                                    itemID,
                                                                    BonusStore.ItemOfferReward(
                                                                        itemType = ItemType.WeaponSkin,
                                                                        itemID = itemID,
                                                                        quantity = 1
                                                                    )
                                                                )
                                                            }
                                                            .build(),
                                                        isSeen = false
                                                    )
                                                )
                                                add(
                                                    BonusStore.ItemOffer(
                                                        bonusOfferID = "1",
                                                        "3",
                                                        isDirectPurchase = false,
                                                        startDate = ISO8601.fromEpochMilli(ISO8601.START_OF_TIME_EPOCH_MILLIS),
                                                        cost = StoreCost(
                                                            ValorantPoint,
                                                            1775
                                                        ),
                                                        rewards = persistentMapOf<String, BonusStore.ItemOfferReward>()
                                                            .builder()
                                                            .apply {
                                                                val itemID = "dbg_wpn_skn_prmm_oni_shorty"
                                                                put(
                                                                    itemID,
                                                                    BonusStore.ItemOfferReward(
                                                                        itemType = ItemType.WeaponSkin,
                                                                        itemID = itemID,
                                                                        quantity = 1
                                                                    )
                                                                )
                                                            }
                                                            .build(),
                                                        discountPercent = 27,
                                                        discountedCost = StoreCost(
                                                            currency = ValorantPoint,
                                                            amount = ceil(((100 - 27) / 100f * 1775)).toLong()
                                                        ),
                                                        isSeen = false
                                                    )
                                                )
                                            }
                                            .build(),
                                        remainingDuration = 11.days + 3.hours + 2.minutes + 1.seconds
                                    )
                                )
                            )
                        )
                    }
                )
            }
        }
    }
}