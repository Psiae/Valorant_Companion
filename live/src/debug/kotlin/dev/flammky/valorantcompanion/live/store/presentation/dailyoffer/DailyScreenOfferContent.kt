package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.compose
import dev.flammky.valorantcompanion.base.referentialEqualityFun
import dev.flammky.valorantcompanion.base.rememberWithEquality
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview.AccessoryOfferPanel
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview.WeaponSkinOfferPanel
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview.WeaponSkinOfferPanelItem
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun DailyOfferScreenContent(
    dailyOfferState: DailyOfferScreenState
) {
    BoxWithConstraints {
        val maxWidth = maxWidth
        val maxHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .localMaterial3Surface()
                .padding(
                    vertical = Material3Theme.dpMarginIncrementsOf(1, maxWidth),
                    horizontal = Material3Theme.dpMarginIncrementsOf(1, maxWidth)
                )
                .verticalScroll(rememberScrollState())
        ) {

            compose {
                val bundleOffer = dailyOfferState.featuredBundle.offer
                    .getOrNull() ?: return@compose
                val bundleCount = run {
                    if (bundleOffer.bundles.isNotEmpty())
                        bundleOffer.bundles.size
                    else
                        1
                }
                Column {
                    BasicText(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = "Featured",
                        style = MaterialTheme3.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Material3Theme.blackOrWhiteContent()
                        )
                    )

                    Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(3)))

                    FeaturedBundleDisplayPager(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                        ,
                        bundleCount = bundleCount,
                        getBundle = { i ->
                            bundleOffer.bundles.getOrNull(i)
                                ?: run {
                                    check(i == 0) {
                                        "Invalid getBundle index ($i) on bundleCount($bundleCount)"
                                    }
                                    bundleOffer.bundle
                                }
                        },
                        pageModifier = { Modifier },
                        pageShape = { RoundedCornerShape(8.dp) },
                        isVisibleToUser = true,
                        showIndicator = true
                    )
                }

                Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(5)))
            }

            compose {
                val skinOffer = dailyOfferState.skinsPanel.offer
                    .getOrNull() ?: return@compose

                WeaponSkinOfferPanel(
                    modifier = Modifier,
                    durationLeft = 15.hours + 10.minutes + 20.seconds,
                    itemKey = rememberWithEquality(
                        key = skinOffer,
                        keyEquality = referentialEqualityFun()
                    ) {
                        Any()
                    },
                    // TODO
                    items = rememberWithEquality(
                        key = skinOffer,
                        keyEquality = referentialEqualityFun()
                    ) {
                        skinOffer.itemOffers
                            .mapTo(
                                destination = persistentListOf<WeaponSkinOfferPanelItem>().builder(),
                                transform = { entry ->
                                    WeaponSkinOfferPanelItem(
                                        uuid = entry.value.reward.itemID,
                                        cost = entry.value.cost
                                    )
                                }
                            )
                            .build()
                    },
                    canOpenDetail = { true },
                    openDetail = {  }
                )

                Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(5)))
            }

            compose {
                val accessoryOffer = dailyOfferState.accessory.offer
                    .getOrNull() ?: return@compose

                AccessoryOfferPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .localMaterial3Surface(),
                    offerKey = rememberWithEquality(
                        key = accessoryOffer,
                        keyEquality = referentialEqualityFun()
                    ) {
                        Any()
                    },
                    offer = rememberWithEquality(
                        key = accessoryOffer,
                        keyEquality = referentialEqualityFun()
                    ) {
                        accessoryOffer
                    },
                    canOpenDetail = false,
                    openDetail = {},
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}