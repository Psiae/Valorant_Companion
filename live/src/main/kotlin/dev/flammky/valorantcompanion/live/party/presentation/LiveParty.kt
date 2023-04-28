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
import dev.flammky.valorantcompanion.auth.riot.RiotGeoRepository
import dev.flammky.valorantcompanion.base.theme.material3.*
import dev.flammky.valorantcompanion.pvp.party.PartyService
import dev.flammky.valorantcompanion.pvp.party.ex.PlayerPartyNotFoundException
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequest
import dev.flammky.valorantcompanion.pvp.player.NameService
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
                        state.userRefresh()
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
                    var isUserOwner = false
                    state.partyDataState.value?.members?.forEach { member ->
                        if (member.puuid == state.userPuuid && member.isOwner) {
                            isUserOwner = true
                        }
                        repeat(5) {
                            key(member.puuid + it) {
                                PartyColumnMemberCard(
                                    modifier = Modifier.height(42.dp),
                                    rememberPartyColumnMemberCardPresenter().present(member)
                                )
                            }
                        }
                    }
                    if (state.partyDataState.value?.members?.isEmpty() != false) {
                        return@Column
                    }
                    Divider(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        color = Material3Theme.surfaceVariantColorAsState().value
                    )
                    QueueIdDropDown(
                        canModify = isUserOwner,
                        currentQueueID = state.partyDataState.value?.matchmakingID ?: PlayerPartyData.UNSET.matchmakingID,
                        eligibles = state.partyDataState.value?.eligible ?: PlayerPartyData.UNSET.eligible,
                        onClick = { id -> /* TODO */ }
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val enabled = isUserOwner &&
                                state.partyDataState.value?.members
                                    ?.takeIf { it.isNotEmpty() }
                                    ?.all { it.isReady }
                                ?: false
                        Button(
                            modifier = Modifier
                                .heightIn(max = 34.dp),
                            contentPadding = PaddingValues(
                                vertical = 5.dp,
                                horizontal = 16.dp
                            ),
                            enabled = enabled,
                            onClick = state::startMatchmaking,
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White,
                            )
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                text = "START",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White.copy(alpha = if (enabled) 1f else 0.38f)
                            )
                        }
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

private fun parseAndNormalizeMatchmakingIdToUiString(id: String?): String {
    if (id == null) return ""
    return when (id.lowercase()) {
        "ggteam" -> "escalation"
        "spikerush" -> "spike rush"
        else -> id
    }
}

@Composable
private fun QueueIdDropDown(
    canModify: Boolean,
    currentQueueID: String,
    eligibles: List<String>,
    onClick: (String) -> Unit
) {
    val expandedState = remember(canModify) {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .clickable(canModify) { expandedState.value = !expandedState.value }
            .padding(2.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = parseAndNormalizeMatchmakingIdToUiString(currentQueueID).uppercase(),
            color = Material3Theme.backgroundContentColorAsState().value
        )
        Spacer(Modifier.width(2.dp))
        Icon(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = dev.flammky.valorantcompanion.base.R.drawable.arrow_drop_down_48px),
            tint = Material3Theme.backgroundContentColorAsState().value,
            contentDescription = "game mode"
        )
        DropdownMenu(
            modifier = Modifier.background(
                if (LocalIsThemeDark.current) {
                    Color(0xFF303030)
                } else {
                    Color.White
                }
            ),
            expanded = expandedState.value,
            onDismissRequest = { expandedState.value = false },
        ) {
            run {
                val enabled = eligibles.any { it.equals("unrated", true) }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "UNRATED",
                            color = if (LocalIsThemeDark.current) {
                                Color.White
                            } else {
                                Color.Black
                            }.let {
                                if (enabled) it else it.copy(alpha = 0.38f)
                            }
                        )
                    },
                    enabled = enabled,
                    onClick = { /*TODO*/ }
                )
            }
            run {
                val enabled = eligibles.any { it.equals("competitive", true) }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "COMPETITIVE",
                            color = if (LocalIsThemeDark.current) {
                                Color.White
                            } else {
                                Color.Black
                            }.let {
                                if (enabled) it else it.copy(alpha = 0.38f)
                            }
                        )
                    },
                    enabled = eligibles.any { it.equals("competitive", true) },
                    onClick = { /*TODO*/ }
                )
            }
            run {
                val enabled = eligibles.any { it.equals("swiftplay", true) }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "SWIFTPLAY",
                            color = if (LocalIsThemeDark.current) {
                                Color.White
                            } else {
                                Color.Black
                            }.let {
                                if (enabled) it else it.copy(alpha = 0.38f)
                            }
                        )
                    },
                    enabled = enabled,
                    onClick = { /*TODO*/ }
                )
            }
            run {
                val enabled = eligibles.any { it.equals("spikerush", true) }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "SPIKE RUSH",
                            color = if (LocalIsThemeDark.current) {
                                Color.White
                            } else {
                                Color.Black
                            }.let {
                                if (enabled) it else it.copy(alpha = 0.38f)
                            }
                        )
                    },
                    enabled = enabled,
                    onClick = { /*TODO*/ }
                )
            }
            run {
                val enabled = eligibles.any { it.equals("deathmatch", true) }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "DEATHMATCH",
                            color = if (LocalIsThemeDark.current) {
                                Color.White
                            } else {
                                Color.Black
                            }.let {
                                if (enabled) it else it.copy(alpha = 0.38f)
                            }
                        )
                    },
                    enabled = enabled,
                    onClick = { /*TODO*/ }
                )
            }
            run {
                val enabled = eligibles.any { it.equals("ggteam", true) }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "ESCALATION",
                            color = if (LocalIsThemeDark.current) {
                                Color.White
                            } else {
                                Color.Black
                            }.let {
                                if (enabled) it else it.copy(alpha = 0.38f)
                            }
                        )
                    },
                    enabled = enabled,
                    onClick = { /*TODO*/ }
                )
            }
        }
    }
}

@Composable
private fun PartyGamePodDropDown(
    currentGamePod: String
) {

}

@Composable
fun rememberLivePartyPresenter(
    partyService: PartyService = getFromKoin(),
    authRepository: RiotAuthRepository = getFromKoin(),
    nameService: NameService = getFromKoin(),
    geoRepository: RiotGeoRepository = getFromKoin()
): LivePartyPresenter {
    return remember(partyService, authRepository, nameService, geoRepository) {
        LivePartyPresenter(authRepository, partyService, nameService, geoRepository)
    }
}

class LivePartyState(
    private val fetch: () -> Deferred<PlayerPartyData>,
    private val startMatchmaking: () -> Deferred<Unit>,
    private val changeQueueGameMode: () -> Deferred<PlayerPartyData>,
    private val coroutineScope: CoroutineScope
) {
    val partyDataState = mutableStateOf<PlayerPartyData?>(null)
    var exceptionMessage by mutableStateOf("")
    var initialRefresh by mutableStateOf(true)
    var userRefreshSlot by mutableStateOf(true)
    var userPuuid by mutableStateOf<String?>(null)
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

    fun userRefresh() {
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

    fun startMatchmaking() {
        if (initialRefresh) return
        if (!userRefreshSlot) return
        coroutineScope.launch {  }
    }

    fun changeQueueGameMode(
        id: String
    ) {
        if (initialRefresh) return
        coroutineScope.launch {
            runCatching {

            }
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
    private val nameService: NameService,
    private val geoRepository: RiotGeoRepository
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
                fetch = {
                    acc?.let {
                        val def = CompletableDeferred<PlayerPartyData>()
                        coroutineScope.launch {
                            def.completeWith(
                                result = runCatching {
                                    val pvpPartyData = partyServiceClient
                                        .fetchSignedInPlayerPartyDataAsync(it.model.id)
                                        .await()
                                    val pvpPartyMemberNameInfo = nameService.getPlayerNameAsync(
                                        GetPlayerNameRequest(
                                            geoRepository.getGeoShardInfo(acc.model.id)?.shard
                                                ?: error("Unable to Retrieve Geo Info"),
                                            pvpPartyData.members.map { it.uuid }
                                        )
                                    ).await()
                                    PlayerPartyData(
                                        matchmakingID = pvpPartyData.matchmakingData.queueId,
                                        members = pvpPartyData.members.map {
                                            val pvpName = pvpPartyMemberNameInfo[it.uuid]
                                            PartyMember(
                                                it.uuid,
                                                it.identity.cardId,
                                                pvpName?.gameName ?: "",
                                                pvpName?.tagLine ?: "",
                                                it.isOwner ?: false,
                                                it.isReady,
                                                it.pings.map { ping -> GamePod(ping.gamePodId, ping.pingMs) }
                                            )
                                        },
                                        eligible = pvpPartyData.eligibleQueues
                                    )
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            )
                        }.invokeOnCompletion { ex -> ex?.let { def.completeExceptionally(it) } }
                        def
                    } ?: CompletableDeferred(PlayerPartyData.UNSET)
                },
                startMatchmaking = {
                    val def = CompletableDeferred<Unit>()
                    if (acc == null) return@LivePartyState def.apply { cancel() }
                    coroutineScope.launch {
                        delay(1000)
                        cancel()
                    }.invokeOnCompletion { ex -> ex?.let { def.completeExceptionally(it) } }
                    def
                },
                changeQueueGameMode = {
                    val def = CompletableDeferred<PlayerPartyData>()
                    if (acc == null) return@LivePartyState def.apply { cancel() }
                    coroutineScope.launch {
                        partyServiceClient
                    }.invokeOnCompletion { ex -> ex?.let { def.completeExceptionally(it) } }
                    def
                },
                coroutineScope
            ).apply {
                userPuuid = acc?.model?.id ?: ""
            }
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