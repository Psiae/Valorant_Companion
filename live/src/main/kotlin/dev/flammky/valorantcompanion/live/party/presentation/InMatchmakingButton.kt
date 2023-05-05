package dev.flammky.valorantcompanion.live.party.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.live.R
import kotlin.time.Duration

@Composable
fun InMatchmakingButton(
    modifier: Modifier,
    state: InMatchmakingButtonState
) = InMatchmakingButton(
    modifier = modifier,
    elapsedTime = state.elapsedTime,
    cancel = state.cancelMatchmaking
)

@Composable
fun InMatchmakingButton(
    modifier: Modifier,
    elapsedTime: Duration,
    cancel: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = cancel)
            .background(Color(0xFFF4444C))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        val contentColor = Color.White

        Text(
            text = "In Queue".uppercase(),
            color = contentColor,
            style = MaterialTheme.typography.labelLarge
        )

        Text(
            text = remember(elapsedTime) {
                val seconds = elapsedTime.inWholeSeconds.coerceAtLeast(0)
                if (seconds > 3600) {
                    String.format(
                        "%02d:%02d:%02d",
                        seconds / 3600,
                        seconds % 3600 / 60,
                        seconds % 60
                    )
                } else {
                    String.format(
                        "%02d:%02d",
                        seconds / 60,
                        seconds % 60
                    )
                }
            },
            color = contentColor,
            style = MaterialTheme.typography.labelLarge
        )

        Icon(
            modifier = Modifier
                .size(24.dp),
            painter = painterResource(id = R.drawable.close_fill0_wght400_grad0_opsz48),
            contentDescription = "cancel",
            tint = contentColor
        )
    }
}