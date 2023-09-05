package dev.flammky.valorantcompanion.live.store.presentation.dailyoffer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.tintElevatedSurface
import dev.flammky.valorantcompanion.base.theme.material3.*
import kotlin.time.Duration

@Composable
fun FeaturedBundleDisplay(
    modifier: Modifier,
    bundleName: String,
    durationLeft: Duration,
    imageKey: Any,
    image: LocalImage<*>,
    shape: Shape,
) {
    Column(
        modifier
            .localMaterial3Surface(
                shape = shape,
                shadowElevation = 3.dp
            )
    ) {

        Row(
            modifier = Modifier
                .background(
                    Material3Theme.foldLightOrDarkTheme(
                        light = {
                            Material3Theme.surfaceColorAsState().value.let { sfc ->
                                remember(sfc) { sfc.tintElevatedSurface(Color.DarkGray, 3.dp) }
                            }
                        },
                        dark = {
                            Material3Theme.surfaceColorAsState().value.let { sfc ->
                                remember(sfc) { sfc.tintElevatedSurface(Color.LightGray, 3.dp) }
                            }
                        }
                    )
                )
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {

            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .align(Alignment.CenterVertically),
                text = bundleName,
                style = MaterialTheme3.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Material3Theme.blackOrWhiteContent()
                )
            )

            BasicText(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                text = remember(durationLeft) {
                    durationLeft.toComponents { days, hours, minutes, seconds, nanoseconds ->
                        "${days}D $hours:$minutes:$seconds"
                    }
                },
                style = MaterialTheme3.typography.labelMedium.copy(
                    fontWeight = FontWeight.Light,
                    color = Material3Theme.blackOrWhiteContent()
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

        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = run {
                val ctx = LocalContext.current
                remember (ctx, imageKey) {
                    ImageRequest.Builder(ctx)
                        .data(image.value)
                        .build()
                }
            },
            contentScale = ContentScale.FillBounds,
            contentDescription = "Featured Bundle"
        )
    }
}