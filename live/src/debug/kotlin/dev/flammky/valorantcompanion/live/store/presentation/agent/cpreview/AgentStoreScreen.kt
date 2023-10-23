package dev.flammky.valorantcompanion.live.store.presentation.agent.cpreview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.assets.debug.DebugValorantAssetService
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.compose.compose
import dev.flammky.valorantcompanion.base.di.compose.LocalDependencyInjector
import dev.flammky.valorantcompanion.base.di.koin.compose.KoinDependencyInjector
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.live.store.presentation.agent.AgentDisplayCard
import dev.flammky.valorantcompanion.live.store.presentation.agent.AgentStoreScreen
import dev.flammky.valorantcompanion.live.store.presentation.agent.present
import dev.flammky.valorantcompanion.live.store.presentation.agent.rememberAgentDisplayCardPresenter
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.store.ItemType
import dev.flammky.valorantcompanion.pvp.store.ValorantStoreService
import dev.flammky.valorantcompanion.pvp.store.debug.StubValorantStoreService
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import kotlin.math.ceil

@Composable
@Preview
fun AgentStoreScreenPreview(
    modifier: Modifier = Modifier
) {
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
                    single<ValorantStoreService> {
                        StubValorantStoreService(
                            entitledItemProvider = { userUUID, itemType ->
                                if (userUUID == "dokka" && itemType == ItemType.Agent) {
                                    return@StubValorantStoreService persistentSetOf(
                                        ValorantAgentIdentity.OMEN.uuid,
                                        ValorantAgentIdentity.JETT.uuid,
                                        ValorantAgentIdentity.SOVA.uuid,
                                        ValorantAgentIdentity.PHOENIX.uuid,
                                        ValorantAgentIdentity.SAGE.uuid
                                    )
                                }
                                return@StubValorantStoreService null
                            }
                        )
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
            AgentStoreScreen(isVisibleToUser = true, userUUID = "dokka")
        }
    }
}