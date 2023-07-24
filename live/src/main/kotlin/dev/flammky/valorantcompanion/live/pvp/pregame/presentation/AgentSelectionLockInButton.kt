package dev.flammky.valorantcompanion.live.pvp.pregame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AgentSelectionLockInButton(
    modifier: Modifier,
    agentName: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF4444C).copy(alpha = if (enabled) 1f else 0.38f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Lock $agentName".uppercase(),
            style = MaterialTheme
                .typography
                .labelLarge
                .copy(
                    color = Color.White.copy(alpha = if (enabled) 1f else 0.38f)
                )
        )
    }
}