package dev.flammky.valorantcompanion.live.ingame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.surfaceVariantColorAsState

@Composable
internal fun LiveInGameTopBar(
    modifier: Modifier,
    mapName: String?,
    gameModeName: String?,
    gamePodName: String?,
    pingMs: Int?,
    mapImage: LocalImage<*>?
) = LiveInGameTopBarContainer(
    modifier = modifier,
    background = @Composable { backgroundModifier ->
        LiveInGameTopBarBackground(modifier = backgroundModifier, mapImage)
    },
    content = @Composable { contentModifier ->
        LiveInGameTopBarContent(
            modifier = contentModifier,
            mapName = mapName,
            gameModeName = gameModeName,
            gamePodName = gamePodName,
            pingMs = pingMs
        )
    }
)

@Composable
private inline fun LiveInGameTopBarContainer(
    modifier: Modifier,
    background: @Composable (Modifier) -> Unit,
    content: @Composable (Modifier) -> Unit
) = Box(modifier = modifier
    .fillMaxWidth()
    .heightIn(min = 42.dp)
) {
    background(Modifier.matchParentSize())
    content(Modifier.padding(5.dp))
}

@Composable
private fun LiveInGameTopBarBackground(
    modifier: Modifier,
    image: LocalImage<*>?
) = AsyncImage(
    modifier = modifier,
    // TODO: map image
    model = image?.value,
    contentDescription = null,
    contentScale = ContentScale.Crop
)

@Composable
private fun LiveInGameTopBarContent(
    modifier: Modifier,
    mapName: String?,
    gameModeName: String?,
    gamePodName: String?,
    pingMs: Int?
) = Column(
    modifier = modifier
        .clip(RoundedCornerShape(15.dp))
        .background(Material3Theme.surfaceVariantColorAsState().value.copy(alpha = 0.97f))
        .padding(12.dp)
) {
    val textColor =
        if (LocalIsThemeDark.current) Color.White else Color.Black

    if (mapName != null) {
        Text(
            text = "Map - $mapName".uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (gameModeName != null) {
        Text(
            text = gameModeName.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (gamePodName != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            Text(
                modifier = Modifier.weight(1f, false),
                text = gamePodName.ifBlank { "???" },
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (pingMs != null) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    modifier = Modifier.weight(1f, false),
                    text = "(${pingMs}ms)",
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(1.dp))
                DrawLiveInGameTopBar4PingBar(modifier = Modifier
                    .height(8.dp)
                    .align(Alignment.CenterVertically), pingMs = pingMs)
            }
        }
    }
}