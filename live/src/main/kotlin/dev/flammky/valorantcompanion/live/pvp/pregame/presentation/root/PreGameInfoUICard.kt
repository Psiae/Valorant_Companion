package dev.flammky.valorantcompanion.live.pvp.pregame.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.R
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundContentColorAsState
import dev.flammky.valorantcompanion.base.theme.material3.surfaceContentColorAsState
import dev.flammky.valorantcompanion.base.theme.material3.surfaceVariantColorAsState

@Composable
fun PreGameInfoUICard(
    mapName: String,
    gameModeName: String,
    gamePodName: String,
    gamePodPingMs: Int,
    openDetail: () -> Unit
) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(15.dp))
        .background(Material3Theme.surfaceVariantColorAsState().value.copy(alpha = 0.80f))
        .clickable(onClick = openDetail)
        .padding(vertical = 8.dp, horizontal = 10.dp),
) {
    Text(
        text = "PRE GAME",
        color = Material3Theme.backgroundContentColorAsState().value,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = Modifier.height(5.dp))
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = Material3Theme.surfaceVariantColorAsState().value.copy(alpha = 0.94f).compositeOver(
            Color.White)
    )
    Spacer(modifier = Modifier.height(5.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "MAP - ${mapName.ifBlank { "Unknown Map Name" }.uppercase()}",
                color = Material3Theme.backgroundContentColorAsState().value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = gameModeName.ifBlank { "Unknown Game Mode Name" }.uppercase(),
                color = Material3Theme.backgroundContentColorAsState().value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (gamePodName.isNotBlank()) {
                    val pingStr = if (gamePodPingMs >= 0) "(${gamePodPingMs}ms)" else "(???)"
                    "$gamePodName $pingStr"
                } else "",
                color = Material3Theme.backgroundContentColorAsState().value,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Icon(
            modifier = Modifier.size(24.dp).align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.right_arrow_100),
            contentDescription = "open detail",
            tint = Material3Theme.surfaceContentColorAsState().value
        )
    }
}