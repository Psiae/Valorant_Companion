package dev.flammky.valorantcompanion.live.party.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.pvp.party.PartyService
import dev.flammky.valorantcompanion.pvp.party.PlayerPartyData as DomainPlayerPartyData
import dev.flammky.valorantcompanion.pvp.party.PlayerPartyMemberData as DomainPlayerPartyMemberData
import dev.flammky.valorantcompanion.pvp.party.ex.PlayerPartyNotFoundException
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.*
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun LiveParty(
    modifier: Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        LiveParty(
            state = rememberLivePartyPresenter().present()
        )
    }
}

@Composable
private fun LiveParty(
    state: LivePartyState
) {
    val isBackgroundDark = LocalIsThemeDark.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .background(
                color = run {
                    if (isBackgroundDark) {
                        val tint = Color.White
                        val surface = Material3Theme.surfaceColorAsState().value
                        remember(tint, surface) {
                            tint
                                .copy(alpha = 0.05f)
                                .compositeOver(surface)
                        }
                    } else {
                        Material3Theme.surfaceColorAsState().value
                    }
                }
            )
            .run {
                shadow(elevation = 1.dp)
            }
            .padding(10.dp)
    ) {
        Row {
            Text(
                text = "Party",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Material3Theme.backgroundContentColorAsState().value
            )
            Spacer(modifier = Modifier.weight(2f))
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        state.userInitiatedRefresh()
                    },
                painter = painterResource(id = dev.flammky.valorantcompanion.base.R.drawable.refresh_fill0_wght400_grad0_opsz48),
                contentDescription = "refresh",
                tint = Material3Theme.backgroundContentColorAsState().value
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Divider(
            Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            color = Material3Theme.surfaceVariantColorAsState().value
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box {
            Box(modifier = Modifier.alpha(if (state.loading) 0.38f else 1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LivePartyColumn(
                        state = rememberLivePartyColumnPresenter().present(
                            userPuuid = state.userPUUID,
                            partyDataState = state.partyDataState
                        )
                    )
                    state.partyDataState.value?.let { partyData ->
                        if (partyData.members.isEmpty()) {
                            return@let
                        }
                        Divider(
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally),
                            color = Material3Theme.surfaceVariantColorAsState().value
                        )
                        LivePartyMatchmakingColumn(
                            state = rememberLivePartyMatchmakingColumnPresenter(userPUUID = state.userPUUID)
                                .present(partyData = partyData)
                        )
                    }
                }
                if (state.exceptionMessage.isNotBlank()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Error: ${state.exceptionMessage}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (state.loading) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp))
                Box(
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp),
                        color = Color.Red,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
    LaunchedEffect(state) { state.initialRefresh() }
}

@Composable
fun rememberLivePartyPresenter(
    partyService: PartyService = getFromKoin(),
    authRepository: RiotAuthRepository = getFromKoin(),
): LivePartyPresenter {
    return remember(partyService, authRepository) {
        LivePartyPresenter(authRepository, partyService)
    }
}

class LivePartyState(
    val userPUUID: String,
    private val fetch: () -> Deferred<PlayerPartyData>,
    private val coroutineScope: CoroutineScope
) {

    val partyDataState = mutableStateOf<PlayerPartyData?>(null)
    var exceptionMessage by mutableStateOf("")
    var matchmakingExceptionMessage by mutableStateOf("")
    var initialRefresh by mutableStateOf(true)
    var userRefreshSlot by mutableStateOf(true)
    var startMatchmakingSlot by mutableStateOf(true)
    val loading by derivedStateOf { initialRefresh || !userRefreshSlot }

    fun initialRefresh() {
        coroutineScope.launch {
            runCatching {
                fetch().await()
            }.onSuccess {
                onRefreshSuccess(it)
            }.onFailure { ex ->
                onRefreshFailure(ex)
            }
        }.invokeOnCompletion { ex ->
            ex?.let { onRefreshFailure(ex) }
            initialRefresh = false
        }
    }

    fun userInitiatedRefresh() {
        if (initialRefresh) return
        if (!userRefreshSlot) return
        userRefreshSlot = false
        exceptionMessage = ""
        coroutineScope.launch {
            runCatching {
                fetch().await()
            }.onSuccess {
                onRefreshSuccess(it)
            }.onFailure { ex ->
                onRefreshFailure(ex)
            }
        }.invokeOnCompletion { ex ->
            ex?.let { onRefreshFailure(ex) }
            userRefreshSlot = true
        }
    }

    fun chainInitiatedRefresh() {
        if (initialRefresh) return
        if (!userRefreshSlot) return
        userRefreshSlot = false
        exceptionMessage = ""
        coroutineScope.launch {
            runCatching {
                fetch().await()
            }.onSuccess {
                onRefreshSuccess(it)
            }.onFailure { ex ->
                onRefreshFailure(ex)
            }
        }.invokeOnCompletion { ex ->
            ex?.let { onRefreshFailure(ex) }
            userRefreshSlot = true
        }
    }

    private fun onRefreshFailure(
        ex: Throwable
    ) {
        run {
            if (ex is PlayerPartyNotFoundException) {
                exceptionMessage = "Could not find Player party, make sure Valorant Client is " +
                        "running with valid connection"
                return@run
            }
            exceptionMessage = ex.cause?.message ?: ex.message ?: "unexpected error occurred"
        }
        partyDataState.value = null
    }

    private fun onRefreshSuccess(
        data: PlayerPartyData
    ) {
        exceptionMessage = ""
        partyDataState.value = data
    }
}

class LivePartyPresenter(
    private val authRepository: RiotAuthRepository,
    private val partyService: PartyService,
) {

    @Composable
    fun present(): LivePartyState {
        val activeAccountState = remember(this) {
            mutableStateOf<AuthenticatedAccount?>(null)
        }
        val partyServiceClient = remember(this) {
            partyService.createClient()
        }
        val coroutineScope = rememberCoroutineScope()
        val state = remember(activeAccountState.value, partyServiceClient) {
            val acc = activeAccountState.value
            LivePartyState(
                acc?.model?.id ?: "",
                fetch = {
                    acc?.let {
                        val def = CompletableDeferred<PlayerPartyData>()
                        coroutineScope.launch {
                            def.completeWith(
                                result = runCatching {
                                    val pvpPartyData = partyServiceClient
                                        .fetchSignedInPlayerPartyDataAsync(it.model.id)
                                        .await()
                                    mapToPlayerPartyData(pvpPartyData)
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            )
                        }.invokeOnCompletion { ex -> ex?.let { def.completeExceptionally(it) } }
                        def
                    } ?: CompletableDeferred(PlayerPartyData.UNSET)
                },
                coroutineScope
            )
        }
        DisposableEffect(
            this,
        ) {
            val listener = ActiveAccountListener { old, new ->
                activeAccountState.value = new
            }
            authRepository.registerActiveAccountChangeListener(
                listener
            )
            onDispose {
                authRepository.unRegisterActiveAccountListener(listener)
                partyServiceClient.dispose()
            }
        }
        return state
    }
}

private fun mapToPlayerPartyData(
    pvp: DomainPlayerPartyData,
): PlayerPartyData {
    return PlayerPartyData(
        pvp.party_id,
        pvp.matchmakingData.queueId,
        persistentListOf<PlayerPartyMemberInfo>().mutate { list ->
            pvp.members.forEach { member -> list.add(mapToPlayerPartyMemberData(member)) }
        },
        pvp.eligibleQueues.toPersistentList(),
        pvp.matchmakingData.preferredGamePods.toPersistentList()
    )
}

private fun mapToPlayerPartyMemberData(
    pvp: DomainPlayerPartyMemberData,
): PlayerPartyMemberInfo {
    return PlayerPartyMemberInfo(
        pvp.uuid,
        pvp.identity.cardId,
        pvp.isOwner ?: false,
        pvp.isReady,
        pvp.pings.map { ping -> GamePodConnection(ping.gamePodId, ping.pingMs) },
    )
}