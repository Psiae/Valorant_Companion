package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.koin.compose.KoinDependencyInjector
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Surface
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.FeaturedBundleDisplayPager
import dev.flammky.valorantcompanion.pvp.store.FeaturedBundleDisplayData
import dev.flammky.valorantcompanion.pvp.store.FeaturedBundleStore
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCurrency
import dev.flammky.valorantcompanion.pvp.store.currency.ValorantPoint
import dev.flammky.valorantcompanion.pvp.store.debug.StubValorantStoreService
import kotlinx.collections.immutable.persistentListOf
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
fun FeaturedBundleDisplayPagerPreview() {

    DefaultMaterial3Theme {

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

        CompositionLocalProvider(LocalDependencyInjector provides remember { KoinDependencyInjector(GlobalContext) }) {
            if (provisioned.value) {
                Box(
                    modifier = Modifier.fillMaxSize().localMaterial3Surface()
                ) {
                    FeaturedBundleDisplayPager(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(16.dp)
                        ,
                        bundleCount = 2,
                        getBundle = {
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
                        },
                        pageModifier = { Modifier },
                        pageShape = { RoundedCornerShape(8.dp) },
                        isVisibleToUser = true,
                        showIndicator = true
                    )
                }
            }
        }
    }
}