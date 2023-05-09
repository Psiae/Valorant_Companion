package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark

@Composable
fun LockInButton(
    modifier: Modifier,
    selectedAgentName: String,
    canLockIn: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(
                enabled = canLockIn,
                onClick = onClick
            )
            .background(
                if (canLockIn) Color(0xFFF4444C)
                else Color.Transparent
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Lock In $selectedAgentName",
            color = run { 
                if (LocalIsThemeDark.current) Color.White else Color.Black
                // lower alpha ?
            },
            style = MaterialTheme.typography.labelMedium
        )
    }
}