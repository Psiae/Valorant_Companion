package dev.flammky.valorantcompanion.live.pregame.presentation

import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.pvp.map.ValorantMapIdentity
import dev.flammky.valorantcompanion.pvp.mode.ValorantGameMode
import dev.flammky.valorantcompanion.pvp.pregame.*
import dev.flammky.valorantcompanion.pvp.pregame.PreGamePlayerState as DomainPreGamePlayerState
import kotlinx.collections.immutable.persistentListOf
import dev.flammky.valorantcompanion.pvp.TeamID as DomainTeamID
import dev.flammky.valorantcompanion.pvp.pregame.PreGameTeam as DomainPreGameTeam
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.nanoseconds
import org.koin.androidx.compose.get as getFromKoin

@Composable
fun rememberLivePreGamePresenter(
    pregameService: PreGameService = getFromKoin()
): LivePreGamePresenter {
    return remember(pregameService) {
        LivePreGamePresenter(pregameService)
    }
}

class LivePreGamePresenter(
    private val pregameService: PreGameService
) {

    @Composable
    fun present(
        puuid: String
    ): LivePreGameUIState {
        return rememberLocalProducer().produceState(puuid = puuid)
    }
    
    @Composable
    private fun rememberLocalProducer(): LivePreGameUIProducer {
        return remember {
            LivePreGameUIProducer()
        }
    }

    private inner class LivePreGameUIProducer(): RememberObserver {

        private val supervisor = SupervisorJob()
        private val _state = mutableStateOf(LivePreGameUIState.UNSET)
        private var _puuid: String = ""
        private var _client: PreGameClient? = null
        private var _supervisor = SupervisorJob(supervisor)
        private var rememberedByComposition = false
        private var forgottenByComposition = false
        private var rememberCoroutineScope: CoroutineScope? = null
        private val gamePodsPings = mutableMapOf<String, Int>()

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
            rememberCoroutineScope = CoroutineScope(SupervisorJob(supervisor) + Dispatchers.Main.immediate)
        }

        private fun dispose() {
            supervisor.cancel()
            _supervisor.cancel()
            _client?.dispose()
        }

        private fun eventSink(
            client: PreGameClient,
            event: LivePreGameUIState.Event
        ) {
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
            _supervisor = SupervisorJob(supervisor)
            newClientSideEffect()
        }

        private fun newClientSideEffect() {
            initialStateRefresh()
        }

        private fun initialStateRefresh() {
            val supervisor = _supervisor
            rememberCoroutineScope!!.launch(supervisor) {
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
                gameModeName = ValorantGameMode.fromQueueID(data.queueId)?.displayName ?: "UNKNOWN_GAME_MODE_NAME",
                gamePodName = toGamePodName(data.gamePodId),
                gamePodPing = gamePodsPings[data.gamePodId] ?: -1,
                countDown = data.phaseTimeRemainingNS.nanoseconds,
                ally = run {
                    val team = data.allyTeam ?: data.teams.find { it.players.any { it.puuid == _puuid } }
                    team?.let { mapToUiPreGameTeam(it) }
                },
                enemy = data.enemyTeam?.let { mapToUiPreGameTeam(it) },
                isProvisioned = data.state == PreGameState.PROVISIONED,
                errorMessage = "",
                eventSink = run {
                    val client = _client!!
                    { event -> eventSink(client, event) }
                }
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
            state.copy(errorMessage = ex.message ?: "unexpected error occurred")
        }

        private fun mutateState(
            supervisor: Job,
            mutate: (LivePreGameUIState) -> LivePreGameUIState
        ) {
            if (supervisor == _supervisor) {
                _state.value = mutate(_state.value)
            }
        }

        private fun setAutoRefresh(
            on: Boolean
        ) {

        }

        private fun onUserRefresh() {

        }
    }
}