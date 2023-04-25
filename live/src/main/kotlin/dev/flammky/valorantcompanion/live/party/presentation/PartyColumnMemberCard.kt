package dev.flammky.valorantcompanion.live.party.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundContentColorAsState

data class PartyColumnMemberCardState(
    val playerCard: Any?,
    val name: String?,
    val tag: String?
) {
}

@Composable
fun PartyColumnMemberCard(
    modifier: Modifier,
    state: PartyColumnMemberCardState
) {
    PlayerCard(
        modifier,
        state.playerCard,
        state.name ?: "",
        state.tag ?: ""
    )
}


@Composable
private fun PlayerCard(
    modifier: Modifier,
    playerCard: Any?,
    name: String,
    tag: String,
) {
    val ctx = LocalContext.current
    Row(modifier) {
        Box(modifier = Modifier.fillMaxHeight().aspectRatio(1f)) {
            AsyncImage(
                modifier = Modifier.align(Alignment.Center),
                model = remember(ctx) {
                    ImageRequest.Builder(ctx)
                        .crossfade(true)
                        .data(playerCard)
                },
                contentDescription = "player card"
            )
        }
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = name,
            color = Material3Theme.backgroundContentColorAsState().value,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(3.dp))

        Row(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .background(
                    if (LocalIsThemeDark.current) {
                        Color.White.copy(alpha = 0.1f)
                    } else {
                        Color.Black.copy(alpha = 0.1f)
                    }
                )
        ) {
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "#$tag",
                color = Material3Theme.backgroundContentColorAsState().value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}