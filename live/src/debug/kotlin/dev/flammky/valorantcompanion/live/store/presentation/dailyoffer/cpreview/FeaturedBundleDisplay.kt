package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.cpreview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.localMaterial3Surface
import dev.flammky.valorantcompanion.live.store.presentation.dailyoffer.FeaturedBundleDisplay
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Preview
@Composable
private fun FeaturedBundleDisplayPreview() {

    DefaultMaterial3Theme(dark = isSystemInDarkTheme()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .localMaterial3Surface()
        ) {
            FeaturedBundleDisplay(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                bundleName = "Neo Frontier".uppercase(),
                durationLeft = 12.days + 10.hours + 35.minutes + 20.seconds,
                imageKey = Any(),
                image = LocalImage.Resource(R_ASSET_RAW.debug_bundle_neo_frontier),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}