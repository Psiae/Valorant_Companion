package dev.flammky.valorantcompanion.live.party.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .background(
                color = run {
                    if (LocalIsThemeDark.current) {
                        Color.White
                    } else {
                        Color.Black
                    }.let { tint ->
                        val surface = Material3Theme.surfaceColorAsState().value
                        remember(tint, surface) {
                            tint
                                .copy(alpha = 0.05f)
                                .compositeOver(surface)
                        }
                    }
                }
            )
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
            Box(modifier = Modifier.alpha(if (state.loading) 0.5f else 1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.partyMembers.value.forEach { member ->
                        repeat(5) {
                            key(member.puuid + it) {
                                PartyColumnMemberCard(
                                    modifier = Modifier.height(42.dp),
                                    rememberPartyColumnMemberCardPresenter().present(
                                        member.puuid,
                                        member.name,
                                        member.tag,
                                        member.cardArtId
                                    )
                                )
                            }
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
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
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
    nameService: NameService = getFromKoin(),
    geoRepository: RiotGeoRepository = getFromKoin()
): LivePartyPresenter {
    return remember(partyService, authRepository, nameService, geoRepository) {
        LivePartyPresenter(authRepository, partyService, nameService, geoRepository)
    }
}

class LivePartyState(
    private val fetch: () -> Deferred<List<PartyMember>>,
    private val coroutineScope: CoroutineScope
) {
    val partyMembers = mutableStateOf<List<PartyMember>>(emptyList())

    var exceptionMessage by mutableStateOf("")
    var initialRefresh by mutableStateOf(true)
    var userRefreshSlot by mutableStateOf(true)
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

    private fun onRefreshFailure(
        ex: Throwable
    ) {
        run {
            if (ex is PlayerPartyNotFoundException) {
                exceptionMessage = "Cannot find Player party, make sure Valorant Client is running with valid connection"
                return@run
            }
            exceptionMessage = ex.cause?.message ?: ex.message ?: "unexpected error occurred"
        }
        partyMembers.value = emptyList()
    }

    private fun onRefreshSuccess(
        members: List<PartyMember>
    ) {
        partyMembers.value = members
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
                        val def = CompletableDeferred<List<PartyMember>>()
                        coroutineScope.launch {
                            def.completeWith(
                                result = runCatching {
                                    val pvpPartyMemberInfo = partyServiceClient
                                        .fetchSignedInPlayerPartyMembersAsync(it.model.id)
                                        .await()
                                    val pvpPartyMemberNameInfo = nameService.getPlayerNameAsync(
                                        GetPlayerNameRequest(
                                            geoRepository.getGeoShardInfo(acc.model.id)?.shard
                                                ?: error("Unable to Retrieve Geo Info"),
                                            pvpPartyMemberInfo.map { it.uuid }
                                        )
                                    ).await()
                                    pvpPartyMemberInfo.map {
                                        val pvpName = pvpPartyMemberNameInfo[it.uuid]
                                        PartyMember(
                                            it.uuid,
                                            it.identity.cardId,
                                            pvpName?.gameName ?: "",
                                            pvpName?.tagLine ?: ""
                                        )
                                    }
                                }.onFailure {
                                    it.printStackTrace()
                                }
                            )
                        }.invokeOnCompletion { ex -> ex?.let { def.completeExceptionally(it) } }
                        def
                    } ?: CompletableDeferred(emptyList())
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