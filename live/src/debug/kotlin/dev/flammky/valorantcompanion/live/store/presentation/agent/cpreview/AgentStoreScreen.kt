package dev.flammky.valorantcompanion.live.store.presentation.agent.cpreview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.assets.R_ASSET_RAW
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.koin.compose.KoinDependencyInjector
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.live.store.presentation.agent.AgentDisplayCard
import dev.flammky.valorantcompanion.live.store.presentation.agent.present
import dev.flammky.valorantcompanion.live.store.presentation.agent.rememberAgentDisplayCardPresenter
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import kotlin.math.ceil

@Composable
@Preview
fun AgentStoreScreenPreview() {
    val provisionedState = remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = Unit, block = {
        provisionedState.value = false
        GlobalContext.stopKoin()
        GlobalContext.startKoin {
            modules(
                module {
                    single<ValorantAssetsService> {
                        DebugValorantAssetService()
                    }
                }
            )
        }
        provisionedState.value = true
    })
    if (!provisionedState.value) return
    CompositionLocalProvider(
        LocalDependencyInjector provides remember {
            KoinDependencyInjector(GlobalContext)
        }
    ) {
        DefaultMaterial3Theme(
            dark = true
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .localMaterial3Surface()
            ) {
                val maxWidth = maxWidth
                Column(
                    modifier = Modifier
                        .padding(Material3Theme.dpMarginIncrementsOf(1, maxWidth))
                        .verticalScroll(rememberScrollState())
                ) {
                    val cellSpacer = Material3Theme.dpPaddingIncrementsOf(2)
                    val cellRowSpacer = Material3Theme.dpPaddingIncrementsOf(2)
                    val cellCountPerRow = 2
                    val cardWidth = ((maxWidth.value - 16 - 8) / 2).dp
                    val cardContentPadding = PaddingValues(Material3Theme.dpPaddingIncrementsOf(2))
                    val agents = remember { ValorantAgentIdentity.asList() }
                    val entitledAgents = remember {
                        listOf(
                            ValorantAgentIdentity.JETT.uuid,
                            ValorantAgentIdentity.SOVA.uuid,
                            ValorantAgentIdentity.PHOENIX,
                            ValorantAgentIdentity.BRIMSTONE,
                            ValorantAgentIdentity.SAGE.uuid
                        )
                    }
                    val cellCount = agents.size
                    val rowCount = ceil(cellCount.toFloat() / 2).toInt()
                    repeat(rowCount) { rowIndex ->
                        Row {
                            val rowCellCount =
                                if (rowIndex == rowCount -1) {
                                    cellCount - cellCountPerRow * (rowCount - 1)
                                } else {
                                    2
                                }
                            repeat(rowCellCount) { cellIndex ->
                                val identity = agents[rowIndex * 2 + cellIndex]
                                val state = rememberAgentDisplayCardPresenter(
                                    dependencyInjector = LocalDependencyInjector.current,
                                ).present(agentUUID = identity.uuid)
                                AgentDisplayCard(
                                    modifier = Modifier.width(cardWidth),
                                    contentPadding = cardContentPadding,
                                    owned = identity.uuid in entitledAgents,
                                    agentDisplayImageModifier = Modifier
                                        .size(cardWidth - 16.dp),
                                    agentDisplayImageKey = state.agentDisplayImageKey,
                                    agentDisplayImage = state.agentDisplayImage,
                                    agentRoleDisplayImageKey = state.agentRoleDisplayImageKey,
                                    agentRoleDisplayImage = state.agentRoleDisplayImage,
                                    agentName = state.agentName,
                                    canOpenDetail = true,
                                    openDetail = {}
                                )
                                if (cellIndex < cellCountPerRow - 1) {
                                    Spacer(modifier = Modifier.width(cellSpacer))
                                }
                            }
                        }
                        if (rowIndex < rowCount - 1) {
                            Spacer(modifier = Modifier.height(cellRowSpacer))
                        }
                    }
                }
            }
        }
    }
}