package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.base.theme.material3.*
import kotlin.time.Duration

@Composable
internal fun LivePreGameContent(
    modifier: Modifier,
    state: LivePreGameScreenState
) = if (state.noOp) {
    LivePreGameContentNoOp(
        modifier = modifier, 
        msg = state.noOpMessage!!, 
        isError = true
    )
} else if (state.needUserRefresh) {
    LivePreGameContentNeedUserRefresh(
        modifier = modifier,
        state.needUserRefreshMessage!!,
        state.needUserRefreshRunnable!!
    )
} else LiveInGamePreGameContent(
    modifier = modifier,
    matchID = state.matchId,
    explicitLoading = state.explicitLoading,
    explicitLoadingMessage = state.explicitLoadingMessage,
    mapName = state.mapName,
    gameTypeName = state.gameTypeName,
    gamePodName = state.gamePodName,
    gamePodPingMs = state.gamePodPingMs,
    countdown = state.countdown,
    allyKey = state.allyKey,
    ally = state.ally,
    user = state.user,
    selectAgent = state.selectAgent,
    lockAgent = state.lockAgent
)

@Composable
private fun LiveInGamePreGameContent(
    modifier: Modifier,
    user: String,
    matchID: String?,
    explicitLoading: Boolean,
    explicitLoadingMessage: String?,
    mapName: String?,
    gameTypeName: String?,
    gamePodName: String?,
    gamePodPingMs: Int?,
    countdown: Duration?,
    allyKey: Any,
    ally: PreGameTeam?,
    selectAgent: (String) -> Unit,
    lockAgent: (String) -> Unit
) = Box(modifier) {
    if (matchID != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
        ) {
            LivePreGameTopBar(
                modifier = Modifier,
                mapName = mapName ?: "",
                gamePodName = gamePodName ?: "",
                gameTypeName = gameTypeName ?: "",
                gamePodPing = gamePodPingMs ?: -1,
                countDown = countdown
            )
            if (ally != null) {
                val userPlayer = remember(allyKey, user) { ally.players.find { it.puuid == user } }
                if (userPlayer != null) {
                    LivePreGameAgentSelectionColumn(
                        modifier = Modifier,
                        user = userPlayer,
                        ally = ally,
                        allyKey = allyKey,
                        matchID = matchID,
                        selectAgent = selectAgent,
                        lockAgent = lockAgent
                    )
                }
            }
        }
    }
    if (explicitLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = run {
                        (if (LocalIsThemeDark.current) Color.Black else Color.White)
                            .copy(alpha = 0.94f)
                    }
                )
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.CenterHorizontally),
                    color = Color.Red,
                    strokeWidth = 2.dp
                )
                if (explicitLoadingMessage != null) {
                    Spacer(Modifier.height(25.dp))
                    Text(
                        text = explicitLoadingMessage,
                        color = if (LocalIsThemeDark.current) Color.White else Color.Black,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun LivePreGameContentNeedUserRefresh(
    modifier: Modifier,
    message: String,
    refresh: () -> Unit
)  = BoxWithConstraints(
    modifier = modifier
        .fillMaxSize()
        .background(
            color = run {
                val tint =
                    if (LocalIsThemeDark.current) Color.Black else Color.White.copy(alpha = 0.94f)
                val base = Material3Theme.backgroundColorAsState().value
                remember(tint, base) { tint.compositeOver(base) }
            }
        )
        .pointerInput(Unit) {}
) {
    val size = remember(maxHeight, maxWidth) {
        minOf(maxWidth * 0.6f, maxHeight * 0.6f).coerceAtLeast(80.dp)
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 20.dp, horizontal = 30.dp)) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "REFRESH MANUALLY TO TRY AGAIN",
            color = if (LocalIsThemeDark.current) {
                Color.White
            } else {
                Color.Black
            },
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(size)
                    .clip(CircleShape)
                    .clickable(enabled = true, onClick = refresh)
                    .run {
                        background(Material3Theme.surfaceVariantColorAsState().value)
                    }
                    .padding(15.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxSize(),
                    painter = painterResource(id = R_ASSET_DRAWABLE.refresh_fill0_wght400_grad0_opsz48),
                    contentDescription = "refresh",
                    tint = Material3Theme.surfaceVariantContentColorAsState().value
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = message,
                color = Material3Theme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}


@Composable
private fun LivePreGameContentNoOp(
    modifier: Modifier,
    msg: String,
    isError: Boolean
) = Box(
    modifier = modifier
        .fillMaxSize()
        .pointerInput(Unit) {}
) {
    Text(
        modifier = Modifier.align(Alignment.Center),
        text = msg,
        color =
        if (isError) MaterialTheme.colorScheme.error
        else (if (LocalIsThemeDark.current) Color.White else Color.Black).copy(alpha = 0.38f),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}