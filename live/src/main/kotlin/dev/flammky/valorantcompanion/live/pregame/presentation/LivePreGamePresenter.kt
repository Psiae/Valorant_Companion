package dev.flammky.valorantcompanion.live.pregame.presentation

import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.pvp.map.ValorantMapIdentity
import dev.flammky.valorantcompanion.pvp.mode.ValorantGameMode
import dev.flammky.valorantcompanion.pvp.pregame.*
import dev.flammky.valorantcompanion.pvp.pregame.ex.PreGameNotFoundException
import dev.flammky.valorantcompanion.pvp.pregame.PreGamePlayerState as DomainPreGamePlayerState
import kotlinx.collections.immutable.persistentListOf
import dev.flammky.valorantcompanion.pvp.TeamID as DomainTeamID
import dev.flammky.valorantcompanion.pvp.pregame.PreGameTeam as DomainPreGameTeam
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun rememberLivePreGamePresenter(
    pregameService: PreGameService = getFromKoin(),
    authRepository: RiotAuthRepository = getFromKoin()
): LivePreGamePresenter {
    return remember(pregameService) {
        LivePreGamePresenter(pregameService, authRepository)
    }
}

class LivePreGamePresenter(
    private val pregameService: PreGameService,
    private val authRepository: RiotAuthRepository,
) {

    @Composable
    fun present(): LivePreGameUIState {
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
    ): LivePreGameUIState {
        return rememberLocalProducer().apply { onRemembered() }.produceState(puuid = puuid)
    }

    @Composable
    private fun rememberLocalProducer(): LivePreGameUIProducer {
        return remember {
            LivePreGameUIProducer()
        }
    }

    // TODO: stateless puuid
    private inner class LivePreGameUIProducer(): RememberObserver {

        private val lifetime = SupervisorJob()
        private val _state = mutableStateOf(LivePreGameUIState.UNSET)
        private var _puuid: String = ""
        private var _client: PreGameClient? = null
        private var _supervisor = SupervisorJob(lifetime)
        private var rememberedByComposition = false
        private var forgottenByComposition = false
        private var rememberCoroutineScope: CoroutineScope? = null
        private val gamePodsPings = mutableMapOf<String, Int>()
        private var userRefresh: Job? = null
        private var autoRefresh: Job? = null
        private var autoRefreshScheduler: Job? = null
        private var lastRefreshStamp: Long = -1

        @Composable
        fun produceState(
            puuid: String
        ): LivePreGameUIState {

            check(rememberedByComposition) {
                "produceState is called before onRemembered"
            }

            check(!forgottenByComposition) {
                "produceState is called after onForgotten"
            }
            
            if (puuid != _puuid) {
                newPuuidSideEffect(puuid)
            }

            return _state.value
        }
        override fun onAbandoned() {
            dispose()
        }

        override fun onForgotten() {
            dispose()
            forgottenByComposition = true
            rememberCoroutineScope!!.cancel()
        }

        override fun onRemembered() {
            if (rememberedByComposition) return
            rememberedByComposition = true
            rememberCoroutineScope = CoroutineScope(SupervisorJob(lifetime) + Dispatchers.Main.immediate)
        }

        private fun dispose() {
            lifetime.cancel()
            _supervisor.cancel()
            _client?.dispose()
        }

        private fun eventSink(
            client: PreGameClient,
            event: LivePreGameUIState.Event
        ) {
            Log.d("LivePreGamePresenter.kt", "eventSink($event)")
            when (event) {
                is LivePreGameUIState.Event.LOCK_AGENT -> client.lockAgent(event.uuid)
                is LivePreGameUIState.Event.SELECT_AGENT -> client.selectAgent(event.uuid)
                is LivePreGameUIState.Event.SET_AUTO_REFRESH -> setAutoRefresh(event.on)
                LivePreGameUIState.Event.USER_REFRESH -> onUserRefresh()
            }
        }

        private fun newPuuidSideEffect(
            puuid: String
        ) {
            _puuid = puuid
            _state.value = LivePreGameUIState.UNSET
            _client?.dispose()
            _client = pregameService.createClient(puuid)
            _supervisor.cancel()
            _supervisor = SupervisorJob(lifetime)
            userRefresh?.cancel()
            autoRefresh?.cancel()
            autoRefreshScheduler?.cancel()
            newClientSideEffect()
        }

        private fun newClientSideEffect() {
            if (_puuid.isBlank()) return
            mutateState(_supervisor) { state ->
                state
                    .copy(
                        eventSink = run {
                            val client = _client!!
                            { event -> eventSink(client, event) }
                        }
                    )
                    // TODO: remove when have toggle
                    .also { it.eventSink(LivePreGameUIState.Event.SET_AUTO_REFRESH(true)) }
            }
            initialStateRefresh()
        }

        private fun initialStateRefresh() {
            val supervisor = _supervisor
            userRefresh = rememberCoroutineScope!!.launch(supervisor) {
                mutateState(supervisor) { state ->
                    state.copy(showLoading = true)
                }
                val def = _client!!.fetchCurrentPreGameMatchData()
                def.await()
                    .onSuccess { data ->
                        newData(supervisor, data)
                    }
                    .onFailure { ex ->
                        fetchFail(supervisor, ex as Exception)
                    }
            }
        }

        private fun newData(
            supervisor: Job,
            data: PreGameMatchData
        ) = mutateState(supervisor) { state ->
            state.copy(
                inPreGame = true,
                mapName = ValorantMapIdentity.fromID(data.mapId)?.display_name ?: "UNKNOWN_MAP_NAME",
                gameModeName = ValorantGameMode.fromQueueID(data.queueId)?.displayName
                    ?: run {
                        if (data.provisioningFlow.lowercase() == "CustomGame".lowercase()) {
                            "Custom Game"
                        } else {
                            "UNKNOWN_GAME_MODE"
                        }
                    },
                gamePodName = toGamePodName(data.gamePodId),
                gamePodPing = gamePodsPings[data.gamePodId] ?: -1,
                countDown = data.phaseTimeRemainingNS.nanoseconds,
                ally = run {
                    val team = data.allyTeam
                        ?: data.teams.find { team ->
                            team.players.any { player -> player.puuid == _puuid }
                        }
                    team?.let { mapToUiPreGameTeam(it) }
                },
                enemy = data.enemyTeam?.let { mapToUiPreGameTeam(it) },
                isProvisioned = data.state == PreGameState.PROVISIONED,
                errorMessage = null,
                showLoading = false,
            )
        }

        private fun toGamePodName(id: String): String {
            val hasPodNum = id.last().isDigit()
            val segTake = if (hasPodNum) 2 else 1
            var seg = 1
            return id.takeLastWhile { char ->
                if (char == '-') seg++
                seg <= segTake
            }
        }

        private fun mapToUiPreGameTeam(
            domain: DomainPreGameTeam
        ): PreGameTeam {
            return PreGameTeam(
                teamID = when (domain.teamID) {
                    DomainTeamID.RED -> TeamID.RED
                    DomainTeamID.BLUE -> TeamID.BLUE
                },
                players = domain.players.mapTo(
                    destination = persistentListOf<PreGamePlayer>().builder(),
                    transform = { player ->
                        PreGamePlayer(
                            puuid = player.puuid,
                            characterID = player.character_id,
                            characterSelectionState = when (player.preGameCharacterSelectionState) {
                                PreGameCharacterSelectionState.NONE -> CharacterSelectionState.NONE
                                PreGameCharacterSelectionState.SELECTED -> CharacterSelectionState.SELECTED
                                PreGameCharacterSelectionState.LOCKED -> CharacterSelectionState.LOCKED
                            },
                            pregamePlayerState = when (player.preGamePlayerState) {
                                DomainPreGamePlayerState.JOINED -> PreGamePlayerState.JOINED
                                is DomainPreGamePlayerState.ELSE -> PreGamePlayerState.ELSE
                            },
                            competitiveTier = player.competitiveTier,
                            identity = PreGamePlayerInfo(
                                player.identity.puuid,
                                player.identity.playerCardId,
                                player.identity.playerTitleId,
                                player.identity.accountLevel,
                                player.identity.preferredBorderId,
                                player.identity.incognito,
                                player.identity.hideAccountLevel
                            ),
                            // TODO
                            seasonalBadgeInfo = SeasonalBadgeInfo.UNSET,
                            isCaptain = player.isCaptain
                        )
                    }
                ).build()
            )
        }

        private fun fetchFail(
            supervisor: Job,
            ex: Exception
        ) = mutateState(supervisor) { state ->
            state.copy(
                inPreGame = false,
                mapName = "",
                gameModeName = "",
                gamePodPing = -1,
                countDown = Duration.INFINITE,
                ally = null,
                enemy = null,
                isProvisioned = false,
                errorMessage = when (ex) {
                    is PreGameNotFoundException -> null
                    else -> "unexpected error occurred"
                },
                showLoading = false,
            )
        }

        private fun mutateState(
            supervisor: Job,
            mutate: (LivePreGameUIState) -> LivePreGameUIState
        ) {
            check(
                Looper.myLooper()
                    ?.let { it == Looper.getMainLooper() } == true)
            if (supervisor == _supervisor) {
                _state.value = mutate(_state.value)
            }
        }

        private fun setAutoRefresh(
            on: Boolean
        ) {
            Log.d("LivePreGamePresenter.kt", "setAutoRefresh($on)")
            check(rememberedByComposition) {
                "setAutoRefresh is called before onRemembered"
            }
            check(!forgottenByComposition) {
                "setAutoRefresh is called after onForgotten"
            }
            if (on) {
                if (autoRefreshScheduler?.isActive == true) return
                autoRefreshScheduler = autoRefreshScheduler()
            } else {
                autoRefreshScheduler?.cancel()
            }
            mutateState(_supervisor) { it.copy(isAutoRefreshOn = autoRefreshScheduler?.isActive == true) }
        }

        private fun onUserRefresh() {
            check(rememberedByComposition) {
                "onUserRefresh is called before onRemembered"
            }
            check(!forgottenByComposition) {
                "onUserRefresh is called after onForgotten"
            }
            if (userRefresh?.isActive == true) return
            if (autoRefresh?.isActive == true) autoRefresh?.cancel()
            val supervisor = _supervisor
            userRefresh = rememberCoroutineScope!!.launch(supervisor) {
                refresh(supervisor, true)
            }
        }

        private fun onAutoRefresh() {
            Log.d("LivePreGamePresenter.kt", "onAutoRefresh()")
            check(rememberedByComposition) {
                "onAutoRefresh is called before onRemembered"
            }
            check(!forgottenByComposition) {
                "onAutoRefresh is called after onForgotten"
            }
            if (userRefresh?.isActive == true) return
            if (autoRefresh?.isActive == true) return
            val supervisor = _supervisor
            autoRefresh = rememberCoroutineScope!!.launch(supervisor) {
                refresh(supervisor, false)
            }
        }

        private suspend fun refresh(
            supervisor: Job,
            showLoading: Boolean
        ) {
            lastRefreshStamp = SystemClock.elapsedRealtime()
            if (showLoading) {
                mutateState(supervisor) { state ->
                    state.copy(showLoading = true)
                }
            }
            val def = _client!!.fetchCurrentPreGameMatchData()
            def.await()
                .onSuccess { data ->
                    newData(supervisor, data)
                }
                .onFailure { ex ->
                    fetchFail(supervisor, ex as Exception)
                }
        }

        private fun autoRefreshScheduler(): Job {
            return rememberCoroutineScope!!.launch(lifetime) {
                while (isActive) {
                    // delay until at least 1 second from the last refresh
                    delay(1000 - (SystemClock.elapsedRealtime() - lastRefreshStamp))
                    onAutoRefresh()
                    runCatching { autoRefresh?.join() }
                }
            }
        }
    }
}