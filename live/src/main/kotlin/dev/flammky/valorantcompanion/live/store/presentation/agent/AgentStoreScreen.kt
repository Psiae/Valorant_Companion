@file:OptIn(ExperimentalMaterial3Api::class)

package dev.flammky.valorantcompanion.live.store.presentation.agent

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.flammky.valorantcompanion.assets.R_ASSET_DRAWABLE
import dev.flammky.valorantcompanion.base.MaterialTheme3
import dev.flammky.valorantcompanion.base.compose.compose
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.referentialEqualityFun
import dev.flammky.valorantcompanion.base.rememberWithEquality
import dev.flammky.valorantcompanion.base.theme.material3.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun AgentStoreScreen(
    isVisibleToUser: Boolean
) {
    AgentStoreScreen(
        state = rememberAgentStoreScreenPresenter(LocalDependencyInjector.current)
            .present(
                di = LocalDependencyInjector.current
            )
    )
}

@Composable
fun AgentStoreScreen(
    isVisibleToUser: Boolean,
    userUUID: String
) {
    AgentStoreScreen(
        state = rememberAgentStoreScreenPresenter(LocalDependencyInjector.current)
            .present(
                userUUID
            )
    )
}

@Composable
fun AgentStoreScreen(
    state: AgentStoreScreenState
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .localMaterial3Surface()
    ) {
        val maxWidth = maxWidth
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(

        )
        Box(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            val headerSpacerPx = remember {
                mutableStateOf(0)
            }
            AgentStoreScreenHeader(
                modifier = Modifier
                    .zIndex(2f)
                    .onSizeChanged { size ->
                        headerSpacerPx.value = size.height
                    },
                scrollBehavior = scrollBehavior
            )
            Column(
                modifier = Modifier
                    .zIndex(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(with(LocalDensity.current) { headerSpacerPx.value.toDp() }))
                if (state.validAgents && state.validEntitledAgents) {
                    AgentStoreScreenContent(
                        modifier = Modifier
                            .padding(Material3Theme.dpMarginIncrementsOf(1, maxWidth))
                            .fillMaxSize(),
                        agents = rememberWithEquality(
                            key = state.agents,
                            keyEquality = referentialEqualityFun(),
                            init = { state.agents.toImmutableList() }
                        ),
                        entitledAgents = state.entitledAgents
                    )
                }
            }
        }
    }
}


@Composable
fun AgentStoreScreenHeader(
    modifier: Modifier,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            BasicText(
                text = "Agent Store",
                style = MaterialTheme3
                    .typography.titleLarge.copy(
                        color = Material3Theme.surfaceContentColorAsState().value
                    )
            )
        },
        navigationIcon = {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { },
                painter = painterResource(id = R_ASSET_DRAWABLE.arrow_left_24px),
                contentDescription = null,
                tint = Material3Theme.surfaceContentColorAsState().value
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Material3Theme.surfaceColorAsState().value,
            scrolledContainerColor = Material3Theme.surfaceColorAtElevation(
                tint = Material3Theme.surfaceTintColorAsState().value,
                elevation = 3.dp
            )
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun AgentStoreScreenContent(
    modifier: Modifier,
    agents: ImmutableList<String>,
    entitledAgents: ImmutableSet<String>
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val maxWidth = maxWidth
        Column(
            modifier = Modifier,
        ) {

            val cellSpacer = Material3Theme.dpPaddingIncrementsOf(2)
            val cellRowSpacer = Material3Theme.dpPaddingIncrementsOf(2)
            val cellCountPerRow = 2
            val cardWidth = maxWidth / 2 - cellSpacer / 2
            val cardContentPadding =
                PaddingValues(Material3Theme.dpPaddingIncrementsOf(2))
            val cellCount = agents.size
            val rowCount = ceil(cellCount.toFloat() / 2).toInt()
            repeat(rowCount) { rowIndex ->
                Row {
                    val rowCellCount =
                        if (rowIndex == rowCount - 1) {
                            cellCount - cellCountPerRow * (rowCount - 1)
                        } else {
                            2
                        }
                    repeat(rowCellCount) { cellIndex ->
                        val uuid = agents[rowIndex * 2 + cellIndex]
                        val state = rememberAgentDisplayCardPresenter(
                            dependencyInjector = LocalDependencyInjector.current,
                        ).present(agentUUID = uuid)
                        AgentDisplayCard(
                            modifier = Modifier.width(cardWidth),
                            contentPadding = cardContentPadding,
                            owned = uuid in entitledAgents,
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