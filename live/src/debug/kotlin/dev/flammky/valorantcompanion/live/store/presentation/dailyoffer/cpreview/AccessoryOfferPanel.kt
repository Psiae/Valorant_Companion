package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.tintElevatedSurface
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.AccessoryOfferPanelState
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.rememberAccessoryOfferPanelPresenter
import dev.flammky.valorantcompanion.pvp.store.AccessoryStore
import dev.flammky.valorantcompanion.pvp.store.currency.KingdomCredit
import dev.flammky.valorantcompanion.pvp.store.currency.RadianitePoint
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCurrency
import dev.flammky.valorantcompanion.pvp.store.currency.ValorantPoint
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
@Preview
private fun AccessoryOfferPanelPreview() {

    DefaultMaterial3Theme {

        Box(modifier = Modifier
            .fillMaxSize()
            .localMaterial3Surface()
            .padding(
                vertical = Material3Theme.dpPaddingIncrementsOf(2),
                horizontal = Material3Theme.dpPaddingIncrementsOf(4)
            )
        ) {

            val currencies = remember {
                mutableListOf(StoreCurrency.KingdomCredit, StoreCurrency.ValorantPoint, StoreCurrency.RadianitePoint)
            }

            val currenciesImage = remember {
                mutableMapOf(
                    StoreCurrency.KingdomCredit to R_ASSET_RAW.currency_kc_displayicon,
                    StoreCurrency.ValorantPoint to R_ASSET_RAW.currency_vp_displayicon,
                    StoreCurrency.RadianitePoint to R_ASSET_RAW.currency_rp_displayicon
                )
            }

            val offers = remember {
                persistentListOf(
                    AccessoryOfferPanelItem(
                        displayImageKey = Any(),
                        displayImage = LocalImage.Resource(R_ASSET_RAW.debug_gunbuddy_ignitiongold_displayicon)
                    ),
                    AccessoryOfferPanelItem(
                        displayImageKey = Any(),
                        displayImage = LocalImage.Resource(R_ASSET_RAW.debug_spray_0x3c_3_transparent)
                    ),
                    AccessoryOfferPanelItem(
                        displayImageKey = Any(),
                        displayImage = LocalImage.Resource(R_ASSET_RAW.debug_playercard_wishingyouhappiness_displayicon)
                    ),
                    AccessoryOfferPanelItem(
                        displayImageKey = Any(),
                        displayImage = LocalImage.Resource(R_ASSET_RAW.debug_acc_playercard_yearone_displayicon)
                    )
                )
            }

            AccessoryOfferPanel(
                modifier = Modifier,
                currencyCount = currencies.size,
                getCurrencyImageKey = { i -> i },
                getCurrencyImage = { i -> LocalImage.Resource(currenciesImage[currencies[i]]!!) },
                offerCount = offers.size,
                getOfferDisplayImageKey = { i -> offers[i].displayImageKey },
                getOfferDisplayImage = { i -> offers[i].displayImage },
                durationLeft = 4.days + 6.hours + 20.minutes,
                canOpenDetail = true,
                openDetail = {},
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun AccessoryOfferPanel(
    modifier: Modifier,
    offerKey: Any,
    offer: AccessoryStore.Offer,
    canOpenDetail: Boolean,
    openDetail: () -> Unit,
    shape: Shape
) {
    val presenter =
        rememberAccessoryOfferPanelPresenter(di = LocalDependencyInjector.current)
    AccessoryOfferPanel(
        modifier = modifier,
        state = presenter.present(offerKey = offerKey, offer = offer),
        canOpenDetail = canOpenDetail,
        openDetail = openDetail,
        shape = shape
    )
}

@Composable
fun AccessoryOfferPanel(
    modifier: Modifier,
    state: AccessoryOfferPanelState,
    canOpenDetail: Boolean,
    openDetail: () -> Unit,
    shape: Shape
) {
    AccessoryOfferPanel(
        modifier = modifier,
        currencyCount = state.currencyCount,
        getCurrencyImageKey = state.getCurrencyImageKey,
        getCurrencyImage = state.getCurrencyImage,
        offerCount = state.offerCount,
        getOfferDisplayImageKey = state.getOfferDisplayImageKey,
        getOfferDisplayImage = state.getOfferDisplayImage,
        durationLeft = state.durationLeft,
        canOpenDetail = canOpenDetail, openDetail = openDetail, shape = shape
    )
}

@Composable
private fun AccessoryOfferPanel(
    modifier: Modifier,
    currencyCount: Int,
    getCurrencyImageKey: (Int) -> Any,
    getCurrencyImage: (Int) -> LocalImage<*>,
    offerCount: Int,
    getOfferDisplayImageKey: (Int) -> Any,
    getOfferDisplayImage: (Int) -> LocalImage<*>,
    durationLeft: Duration,
    canOpenDetail: Boolean,
    openDetail: () -> Unit,
    shape: Shape
) {
    val ctx = LocalContext.current
    Column(modifier) {

        Row {

            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .align(Alignment.CenterVertically),
                text = "Accessories",
                style = MaterialTheme3.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Material3Theme.blackOrWhiteContent()
                )
            )

            BasicText(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                text = remember(durationLeft) {
                    durationLeft.toComponents { days, hours, minutes, seconds, _ ->
                        val daysStr = "${days}d"
                        val hoursStr =
                            if (hours >= 10) hours.toString() else "0$hours"
                        val minutesStr =
                            if (minutes >= 10) minutes.toString() else "0$minutes"
                        val secondsStr =
                            if (seconds >= 10) seconds.toString() else "0$seconds"
                        "$daysStr $hoursStr:${minutesStr}:$secondsStr"
                    }
                },
                style = MaterialTheme3.typography.labelLarge.copy(
                    color = Material3Theme.surfaceContentColorAsState().value
                )
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R_ASSET_DRAWABLE.time_machine_ios_16_glyph_100px),
                contentDescription = null,
                tint = Material3Theme.blackOrWhiteContent()
            )
        }

        Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(3)))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .localMaterial3Surface(
                    shadowElevation = 3.dp,
                    shape = shape
                )
                .background(
                    color = Material3Theme.foldLightOrDarkTheme(
                        light = {
                            Material3Theme.surfaceColorAsState().value.let { sfc ->
                                remember(sfc) { sfc.tintElevatedSurface(Color.Gray, 3.dp) }
                            }
                        },
                        dark = {
                            Material3Theme.surfaceColorAsState().value.let { sfc ->
                                remember(sfc) { sfc.tintElevatedSurface(Color.White, 3.dp) }
                            }
                        }
                    )
                )
                .clickable(enabled = canOpenDetail, onClick = openDetail)
                .padding(Material3Theme.dpPaddingIncrementsOf(2))
        ) {

            Row {

                Row(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    repeat(currencyCount) { i ->
                        Icon(
                            modifier = Modifier
                                .size(24.dp),
                            painter = rememberAsyncImagePainter(
                                model = remember(ctx, getCurrencyImageKey(i)) {
                                    ImageRequest.Builder(ctx)
                                        .data(getCurrencyImage(i).value)
                                        .build()
                                }
                            ),
                            contentDescription = null,
                            tint = Material3Theme.surfaceContentColorAsState().value
                        )
                        if (i < currencyCount -1 ) {
                            Spacer(
                                modifier = Modifier
                                    .width(Material3Theme.dpPaddingIncrementsOf(2))
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.width(
                        Material3Theme.dpPaddingIncrementsOf(2)
                    )
                )

                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .interactiveUiElementAlphaEnforcement(true, canOpenDetail),
                    painter = painterResource(id = R_ASSET_DRAWABLE.right_arrow_100),
                    contentDescription = null,
                    tint = Material3Theme.surfaceContentColorAsState().value
                )
            }   

            Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(2)))

            Divider(
                color = Material3Theme.outlineVariantColorAsState().value
            )

            Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(4)))

            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {

                repeat(offerCount) { i ->
                    val key = getOfferDisplayImageKey(i)
                    AsyncImage(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        model = remember(key, ctx) {
                            ImageRequest.Builder(ctx)
                                .data(getOfferDisplayImage(i).value)
                                .build()
                        },
                        contentDescription = null
                    )

                    if (i < offerCount - 1) {
                        Spacer(modifier = Modifier.width(Material3Theme.dpPaddingIncrementsOf(2)))
                    }
                }
            }
        }
    }
}

class AccessoryOfferPanelItem(
    val displayImageKey: Any,
    val displayImage: LocalImage<*>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccessoryOfferPanelItem) return false
        return displayImageKey == other.displayImageKey
    }

    override fun hashCode(): Int {
        var result = 0
        result = displayImageKey.hashCode()
        return result
    }
}