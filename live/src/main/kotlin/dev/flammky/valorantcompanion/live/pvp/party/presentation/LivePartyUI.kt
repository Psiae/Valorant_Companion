package dev.flammky.valorantcompanion.live.pvp.party.presentation

import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.pvp.party.PartyService
import dev.flammky.valorantcompanion.pvp.party.PartyState
import dev.flammky.valorantcompanion.pvp.ex.PlayerNotFoundException
import dev.flammky.valorantcompanion.pvp.party.PlayerPartyData as DomainPlayerPartyData
import dev.flammky.valorantcompanion.pvp.party.PlayerPartyMemberData as DomainPlayerPartyMemberData
import dev.flammky.valorantcompanion.pvp.party.ex.PlayerPartyNotFoundException
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun LivePartyUI(
    modifier: Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        LivePartyUI(
            padding = PaddingValues(15.dp),
            state = rememberLivePartyPresenter().present()
        )
    }
}

@Composable
private fun LivePartyUI(
    state: LivePartyState,
    padding: PaddingValues,
) {
    val isBackgroundDark = LocalIsThemeDark.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding)
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
            .shadow(elevation = 1.dp)
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = "Refresh manually to try again",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            modifier = Modifier,
                            text = "Error: ${state.exceptionMessage}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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
    LaunchedEffect(state) {
        Log.d("live.LiveParty.kt", "LiveParty() { LaunchedEffect($state) }")
        state.initialRefresh()
        snapshotFlow { state.initialRefresh }.first { !it }

        var job: Job? = null
        var stamp = SystemClock.elapsedRealtime()
        runCatching {
            snapshotFlow { state.autoRefreshOn && state.exceptionMessage.isEmpty() }
                .distinctUntilChanged()
                .collect { on ->
                    job?.cancel()
                    if (on) job = launch {
                        // delay until at least 1 second from the last refresh
                        delay(1000 - (SystemClock.elapsedRealtime() - stamp))
                        while (isActive) {
                            stamp = SystemClock.elapsedRealtime()
                            state.autoInitiatedRefresh()
                            delay(1000)
                        }
                    }
                }
        }
        Log.d("live.LiveParty.kt", "LiveParty() { LaunchedEffect($state) } end")
    }
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
    var autoRefreshOn by mutableStateOf(true)
    var isUserRefreshing by mutableStateOf(false)
    val loading by derivedStateOf { initialRefresh || isUserRefreshing }

    fun initialRefresh() {
        if (!initialRefresh) return
        if (!userRefreshSlot) return
        userRefreshSlot = false
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
            userRefreshSlot = true
        }
    }

    fun userInitiatedRefresh() {
        if (initialRefresh) return
        if (!userRefreshSlot) return
        isUserRefreshing = true
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
            isUserRefreshing = false
        }
    }

    fun autoInitiatedRefresh() {
        Log.d("live.LiveParty.kt", "autoInitiatedRefresh")
        if (initialRefresh) return
        if (!userRefreshSlot) return
        // exception should be removed by user initiating the refresh
        if (exceptionMessage.isNotEmpty()) return
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
            } else if (ex is PlayerNotFoundException) {
                exceptionMessage = "Could not find Player resource, try entering a match then try again"
                return@run
            }
            exceptionMessage = ex.message ?: "unexpected error occurred"
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
            }
        }
        return present(puuid = activeAccountState.value?.model?.id ?: "")
    }

    @Composable
    fun present(
        puuid: String
    ): LivePartyState {
        val coroutineScope = rememberCoroutineScope()
        val partyServiceClient = remember(this) {
            partyService.createClient()
        }
        DisposableEffect(
            this,
        ) {
            onDispose {
                partyServiceClient.dispose()
            }
        }
        return remember(puuid, partyServiceClient) {
            LivePartyState(
                puuid,
                fetch = {
                    puuid.takeIf { it.isNotBlank() }?.let {
                        val def = CompletableDeferred<PlayerPartyData>()
                        coroutineScope.launch {
                            def.completeWith(
                                result = runCatching {
                                    val pvpPartyData = partyServiceClient
                                        .fetchSignedInPlayerPartyDataAsync(puuid)
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
    }
}

private fun mapToPlayerPartyData(
    pvp: DomainPlayerPartyData,
): PlayerPartyData {
    return PlayerPartyData(
        partyID = pvp.party_id,
        matchmakingQueueID = pvp.matchmakingData.queueId,
        members = persistentListOf<PlayerPartyMemberInfo>().mutate { list ->
            pvp.members.forEach { member -> list.add(mapToPlayerPartyMemberData(member)) }
        },
        eligible = pvp.eligibleQueues.toPersistentList(),
        preferredPods = pvp.matchmakingData.preferredGamePods.toPersistentList(),
        inQueue = pvp.state == PartyState.toPartyDataString(PartyState.MATCHMAKING),
        timeStamp = runCatching { Instant.parse(pvp.queueEntryTime).toEpochMilliseconds().milliseconds }
            .onFailure {
                it.printStackTrace()
            }
            .getOrElse { Duration.ZERO }
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