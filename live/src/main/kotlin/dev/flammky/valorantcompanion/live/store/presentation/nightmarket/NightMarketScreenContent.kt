package dev.flammky.valorantcompanion.live.store.presentation.nightmarket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.pvp.store.BonusStore
import dev.flammky.valorantcompanion.pvp.store.FeaturedBundleStore
import kotlin.time.Duration

@Composable
internal fun NightMarketScreenContent(
    modifier: Modifier,
    state: NightMarketScreenState
) {
    BoxWithConstraints(modifier) {
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
        ) {
            if (state.bonusStore.open) {
                val offer = state.bonusStore.offer.getOrNull()
                    ?: return@Column
                Column {
                    NightMarketScreenContentHeader(
                        modifier = Modifier,
                        durationLeft = offer.remainingDuration
                    )
                    Spacer(modifier = Modifier.height(Material3Theme.dpPaddingIncrementsOf(2)))
                    NightMarketScreenContentOffer(offer = offer)
                }
            }
        }
    }
}

@Composable
private fun NightMarketScreenContentHeader(
    modifier: Modifier,
    durationLeft: Duration
) {
    Row(modifier) {

        BasicText(
            modifier = Modifier.weight(1f),
            text = "Night Market",
            style = MaterialTheme3.typography.titleMedium.copy(
                color = Material3Theme.surfaceContentColorAsState().value
            )
        )

        Row(modifier = Modifier.align(Alignment.CenterVertically)) {

            BasicText(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = remember(durationLeft) {
                    durationLeft.toComponents { _, hours, minutes, seconds, _ ->
                        val hoursStr = "${hours}h"
                        val minutesStr =
                            if (minutes >= 10) minutes.toString() else "0$minutes"
                        val secondsStr =
                            if (seconds >= 10) seconds.toString() else "0$seconds"
                        "$hoursStr ${minutesStr}:$secondsStr"
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

@Composable
private fun NightMarketScreenContentOffer(
    offer: BonusStore.Offer
) {

}