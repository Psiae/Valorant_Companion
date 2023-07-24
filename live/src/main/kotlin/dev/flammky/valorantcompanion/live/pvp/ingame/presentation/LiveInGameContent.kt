package dev.flammky.valorantcompanion.live.pvp.ingame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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

@Composable
internal fun LiveInGameContent(
    modifier: Modifier,
    state: LiveInGameScreenState
) = if (state.noOp) {
    LiveInGameContentNoOp(
        modifier,
        state.noOpMessage!!,
        state.noOpError
    )
} else if (state.needUserRefresh) {
    LiveInGameContentNeedUserRefresh(
        modifier,
        state.needUserRefreshMessage!!,
        state.needUserRefreshRunnable!!
    )
} else {
    Box {
        Column(modifier = modifier.fillMaxSize()) {
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
                                else Color.White).copy(
                            alpha = 0.94f
                        )
                    )
            )
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
                if (state.explicitLoadingMessage != null) {
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

@Composable
private fun LiveInGameContentNeedUserRefresh(
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
private fun LiveInGameContentNoOp(
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