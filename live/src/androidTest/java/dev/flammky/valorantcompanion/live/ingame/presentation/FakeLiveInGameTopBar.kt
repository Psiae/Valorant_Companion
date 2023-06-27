package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.base.theme.material3.DefaultMaterial3Theme
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceVariantColorAsState
import dev.flammky.valorantcompanion.live.pingStrengthInRangeOf4
import dev.flammky.valorantcompanion.assets.R as R_ASSET

@Composable
@Preview
internal fun FakeLiveInGameTopBarPreview() = DefaultMaterial3Theme(dark = true) {
    FakeLiveInGameTopBar()
}


@Composable
internal fun FakeLiveInGameTopBar() = FakeLiveInGameTopBarContainer(
    background = @Composable { backgroundModifier ->
        FakeLiveInGameTopBarBackground(modifier = backgroundModifier)
    },
    content = @Composable { contentModifier ->
        FakeLiveInGameTopBarContent(modifier = contentModifier)
    }
)

@Composable
private inline fun FakeLiveInGameTopBarContainer(
    background: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit
) = Box(modifier = Modifier
    .fillMaxWidth()
    .heightIn(min = 42.dp)) {
    background(Modifier.matchParentSize())
    content(Modifier.padding(5.dp))
}

@Composable
private fun FakeLiveInGameTopBarBackground(
    modifier: Modifier
) = AsyncImage(
    modifier = modifier,
    model = fakeLiveInGameTopBarBackgroundCoilImageModel(),
    contentDescription = null,
    contentScale = ContentScale.Crop
)

@Composable
private fun FakeLiveInGameTopBarContent(
    modifier: Modifier
) = Column(
    modifier = modifier
        .clip(RoundedCornerShape(15.dp))
        .background(Material3Theme.surfaceVariantColorAsState().value.copy(alpha = 0.97f))
        .padding(12.dp)
) {
    val textColor = if (LocalIsThemeDark.current) Color.White else Color.Black
    Text(
        text = "Map - Ascent".uppercase(),
        color = textColor,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        text = "Spike Rush",
        color = textColor,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row {
        Text(
            modifier = Modifier.weight(1f, false),
            text = "Singapore-1",
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (31 > -1) {
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                modifier = Modifier.weight(1f, false),
                text = "(31ms)",
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(1.dp))
            DrawLiveInGameTopBar4PingBar(modifier = Modifier
                .height(8.dp)
                .align(Alignment.CenterVertically), pingMs = 31)
        }
    }
}


@Composable
private fun fakeLiveInGameTopBarBackgroundCoilImageModel(): ImageRequest {
    val ctx = LocalContext.current
    return remember {
        ImageRequest.Builder(ctx)
            .data(R_ASSET.raw.ascent_listviewicon)
            .build()
    }
}

@Composable
private fun DrawLiveInGameTopBar4PingBar(
    modifier: Modifier,
    pingMs: Int
) {
    val strength = pingStrengthInRangeOf4(pingMs)
    check(strength in 1..4)
    val color = when (strength) {
        1 -> Color.Red
        2 -> Color.Yellow
        3, 4 -> Color.Green
        else -> error("Unguarded condition")
    }
    BoxWithConstraints(
        modifier = modifier.size(24.dp)
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(4) { i ->
                val n = i + 1
                val height = (maxHeight.value * (n.toFloat() / 4)).dp
                val width = maxHeight / 4
                Box(
                    modifier = Modifier
                        .height(height)
                        .width(width)
                        .background(color.takeIf { strength >= n } ?: Color.Gray)
                        .shadow(2.dp)
                )
            }
        }
    }
}