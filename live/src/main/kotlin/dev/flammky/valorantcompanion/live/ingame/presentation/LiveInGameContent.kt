package dev.flammky.valorantcompanion.live.ingame.presentation

import android.app.PendingIntent
import android.net.ConnectivityManager
import android.net.NetworkRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark

@Composable
internal fun LiveInGameContent(
    modifier: Modifier,
    state: LiveInGameScreenState
) = Box {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (state.matchKey != null) {
            LiveInGameTopBar(
                modifier = Modifier,
                mapName = state.mapName,
                gameModeName = state.gameTypeName,
                gamePodName = state.gamePodName,
                pingMs = state.gamePodPingMs?.takeIf { it > -1 },
                // TODO
                mapImage = null
            )
            LiveInGameTeamMembersUI(
                user = state.user,
                matchKey = state.matchKey,
                loading = state.explicitLoading,
                allyProvided = state.allyMembersProvided,
                enemyProvided = state.enemyMembersProvided,
                ally = state.ally,
                enemy = state.enemy
            )
        }
    }
    if (state.explicitLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = (
                        if (LocalIsThemeDark.current) Color.Black
                        else Color.White).copy(alpha = 0.94f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp).align(Alignment.CenterHorizontally),
                color = Color.Red,
                strokeWidth = 2.dp
            )
            if (state.explicitLoadingMessage != null) {
                key(state.explicitLoadingMessage) {
                    Spacer(Modifier.height(25.dp))
                    Text(
                        text = state.explicitLoadingMessage,
                        color = if (LocalIsThemeDark.current) Color.White else Color.Black,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}