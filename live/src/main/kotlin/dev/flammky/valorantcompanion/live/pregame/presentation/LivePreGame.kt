package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.base.theme.material3.LocalIsThemeDark
import dev.flammky.valorantcompanion.base.theme.material3.Material3Theme
import dev.flammky.valorantcompanion.base.theme.material3.backgroundColorAsState
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Composable
fun LivePreGame(
    modifier: Modifier,
    state: LivePreGameUIState
) {
    LivePreGamePlacement(
        modifier,
        Background = { Background(state, TODO()) },
        TopBarInfo = { TopBarInfo(state) },
        AgentSelectionColumn = {
            AgentSelectionColumn(
                state = rememberAgentSelectionPresenter().present(state.ally, state.enemy)
            )
        }
    )
}

@Composable
private fun LivePreGamePlacement(
    modifier: Modifier,
    Background: @Composable () -> Unit,
    TopBarInfo: @Composable () -> Unit,
    AgentSelectionColumn: @Composable () -> Unit
) {
    Box {
        Background()
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Material3Theme.backgroundColorAsState().value)
        ) {
            TopBarInfo()
            AgentSelectionColumn()
        }
    }
}

@Composable
private fun Background(
    state: LivePreGameUIState,
    assetsService: ValorantAssetsService
) {
    val mapName = state.mapName
    val ctx = LocalContext.current
    val dataState = remember(mapName, assetsService) {
        mutableStateOf<Any?>(null)
    }
    val dataKeyState = remember(dataState) {
        mutableStateOf<Any?>(null)
    }
    val client = remember(assetsService) {
        assetsService.createLoaderClient()
    }
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(
        key1 = client,
        effect = {
            onDispose { client.dispose() }
        }
    )
    DisposableEffect(
        key1 = mapName,
        effect = {
            val supervisor = SupervisorJob()
            coroutineScope.launch(supervisor) {
                client
            }


            onDispose {  }
        }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AsyncImage(
            model = remember(ctx, dataKeyState.value) {
                ImageRequest.Builder(ctx)
                    .data(dataState.value)
                    .build()
            },
            contentDescription = mapName.takeIf { it.isNotBlank() }
        )
    }
}

@Composable
private fun TopBarInfo(
    state: LivePreGameUIState
) {
    val textColor = if (LocalIsThemeDark.current) Color.White else Color.Black
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "Agent Select",
            color = textColor
        )
        Text(
            state.mapName,
            color = textColor
        )
        Text(
            state.gameModeName,
            color = textColor
        )
        Text(
            state.gamePodName,
            color = textColor
        )
    }
}