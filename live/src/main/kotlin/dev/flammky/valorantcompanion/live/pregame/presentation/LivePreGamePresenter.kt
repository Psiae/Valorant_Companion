package dev.flammky.valorantcompanion.live.pregame.presentation

import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.live.shared.presentation.LocalImageData
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
        return rememberLocalProducer(puuid).produceState()
    }

    @Composable
    private fun rememberLocalProducer(
        puuid: String
    ): LivePreGameUIProducer {
        return remember(puuid) {
            LivePreGameUIProducer(puuid)
        }
    }

    private inner class LivePreGameUIProducer(
        val puuid: String
    ): RememberObserver {

        private val lifetime = SupervisorJob()
        private val state = mutableStateOf(LivePreGameUIState.UNSET)
        private var client: PreGameClient? = null
        private var rememberedByComposition = false
        private var forgottenByComposition = false
        private var rememberCoroutineScope: CoroutineScope? = null
        private val gamePodsPings = mutableMapOf<String, Int>()
        private var initialDataRefresh: Job? = null
        private var userDataRefresh: Job? = null
        private var autoDataRefresh: Job? = null
        private var latestDataRefresh: Job? = null
        private var autoDataRefreshScheduler: Job? = null
        private var pingRefresh: Job? = null
        private var lastDataRefreshStamp: Long = -1
        private var lastPingRefreshStamp: Long = -1
        private var pingRefreshPolls = 0
            set(value) {
                check(value >= 0) {
                    "pingRefreshPolls should not be set to negative value=$value, field=$field"
                }
                field = value
            }


        @Composable
        fun produceState(): LivePreGameUIState {

            check(!forgottenByComposition) {
                "produceState is called after onForgotten"
            }

            // remember callback is invoked after successful composition
            // call it here to prepare immediately
            onRemembered()

            if (client == null) {
                prepare()
            }

            return state.value
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
            client?.dispose()
        }

        private fun eventSink(
            client: PreGameClient,
            event: LivePreGameUIState.Event,
            matchID: String = "",
        ) {
            Log.d("LivePreGamePresenter.kt", "eventSink($event)")
            when (event) {
                is LivePreGameUIState.Event.LOCK_AGENT -> client.lockAgent(matchID, event.uuid)
                is LivePreGameUIState.Event.SELECT_AGENT -> client.selectAgent(matchID, event.uuid)
                is LivePreGameUIState.Event.SET_AUTO_REFRESH -> setAutoRefresh(event.on)
                LivePreGameUIState.Event.USER_REFRESH -> onUserRefresh()
            }
        }

        private fun prepare() {
            client = pregameService.createClient(puuid)
            state.value = initialState()
            initialStateRefresh()
        }

        private fun initialState(): LivePreGameUIState {
            return LivePreGameUIState.UNSET
                .copy(
                    eventSink = run {
                        val client = client!!
                        { event -> eventSink(client, event) }
                    },
                    dataModContinuationKey = Any()
                )
                // TODO: remove when have toggle
                .also { it.eventSink(LivePreGameUIState.Event.SET_AUTO_REFRESH(true)) }
        }

        private fun initialStateRefresh() {
            initialDataRefresh = rememberCoroutineScope!!.launch(lifetime) {
                refresh("initialStateRefresh", true)
            }.also {
                latestDataRefresh = it
            }
        }

        private fun newData(
            data: PreGameMatchData
        ) = mutateState("newData") { state ->
            val ally = run {
                val team = data.allyTeam
                    ?: data.teams.find { team ->
                        team.players.any { player -> player.puuid == puuid }
                    }
                team?.let { mapToUiPreGameTeam(it) }
            }
            state.copy(
                inPreGame = true,
                matchID = data.match_id,
                mapName = ValorantMapIdentity.fromID(data.mapId)?.display_name ?: "UNKNOWN_MAP_NAME",
                mapId = data.mapId,
                gameModeName = ValorantGameMode.fromQueueID(data.queueId)?.displayName
                    ?: run {
                        if (data.provisioningFlow.lowercase() == "CustomGame".lowercase()) {
                            "Custom Game"
                        } else {
                            "UNKNOWN_GAME_MODE"
                        }
                    },
                gamePodId = data.gamePodId,
                gamePodName = toGamePodName(data.gamePodId),
                gamePodPing = gamePodsPings[data.gamePodId] ?: -1,
                countDown = data.phaseTimeRemainingNS.nanoseconds,
                ally = ally,
                enemy = data.enemyTeam?.let { mapToUiPreGameTeam(it) },
                user = ally?.players?.find { it.puuid == puuid },
                isProvisioned = data.state == PreGameState.PROVISIONED,
                errorMessage = null,
                showLoading = false,
                eventSink = run {
                    val client = client!!
                    { event -> eventSink(client, event, data.match_id) }
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
            ex: Exception
        ) = mutateState("fetchFail") { state ->
            state.copy(
                inPreGame = false,
                mapName = "",
                gamePodId = "",
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
                eventSink = run {
                    val client = client!!
                    { event -> eventSink(client, event) }
                }
            )
        }

        private fun mutateState(
            causeActionName: String,
            mutate: (LivePreGameUIState) -> LivePreGameUIState
        ) {
            Log.d("LivePreGamePresenter.kt", "LivePreGameUIProducer_mutateState($causeActionName)")
            expectMainThread("mutateState, cause=$causeActionName")
            val current = state.value
            val next = mutate(current)
            state.value = next
                .copy(dataMod = current.dataMod + 1)
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
                if (autoDataRefreshScheduler?.isActive == true) return
                autoDataRefreshScheduler = autoRefreshScheduler()
            } else {
                autoDataRefreshScheduler?.cancel()
            }
            mutateState("setAutoRefresh") { state ->
                state.copy(isAutoRefreshOn = autoDataRefreshScheduler?.isActive == true)
            }
        }

        private fun onUserRefresh() {
            check(rememberedByComposition) {
                "onUserRefresh is called before onRemembered"
            }
            check(!forgottenByComposition) {
                "onUserRefresh is called after onForgotten"
            }
            if (initialDataRefresh?.isActive == true) return
            if (userDataRefresh?.isActive == true) return
            if (autoDataRefresh?.isActive == true) autoDataRefresh?.cancel()
            userDataRefresh = rememberCoroutineScope!!.launch(lifetime) {
                refresh("onUserRefresh",true)
            }.also {
                latestDataRefresh = it
            }
        }

        private fun loadMapAsset(def: CompletableDeferred<LocalImageData<*>>) {

        }

        private fun onAutoRefresh() {
            Log.d("LivePreGamePresenter.kt", "onAutoRefresh() @${System.identityHashCode(this)}")
            check(rememberedByComposition) {
                "onAutoRefresh is called before onRemembered"
            }
            check(!forgottenByComposition) {
                "onAutoRefresh is called after onForgotten"
            }
            if (initialDataRefresh?.isActive == true) return
            if (userDataRefresh?.isActive == true) return
            if (autoDataRefresh?.isActive == true) return
            autoDataRefresh = rememberCoroutineScope!!.launch(lifetime) {
                refresh("onAutoRefresh", false)
            }.also {
                latestDataRefresh = it
            }
        }

        private suspend fun refresh(
            actionName: String,
            showLoading: Boolean
        ) {
            lastDataRefreshStamp = SystemClock.elapsedRealtime()
            if (showLoading) {
                mutateState("refreshAction by $actionName") { state ->
                    state.copy(showLoading = true)
                }
            }
            val def = client!!.fetchCurrentPreGameMatchData()
            def.await()
                .onSuccess { data ->
                    newData(data)
                    pollPingRefresh()
                }
                .onFailure { ex ->
                    fetchFail(ex as Exception)
                    cancelPingRefresh()
                }
        }

        private fun pollPingRefresh() {
            expectMainThread("pollPingRefresh")
            Log.d("LivePreGamePresenter.kt", "LivePreGameUIProducer_pollPingRefresh: incrementing poll from $pingRefreshPolls")
            if (pingRefreshPolls++ == 0) {
                Log.d("LivePreGamePresenter.kt", "LivePreGameUIProducer_pollPingRefresh: dispatchingPingRefresh as poll ($pingRefreshPolls) is now 1")
                dispatchPingRefresh()
            }
        }

        private fun cancelPingRefresh() {
            expectMainThread("clearPingRefresh")
            Log.d("LivePreGamePresenter.kt", "LivePreGameUIProducer_pollPingRefresh: cancelling poll at $pingRefreshPolls polls")
            pingRefresh?.cancel()
            pingRefreshPolls = 0
            Log.d("LivePreGamePresenter.kt", "LivePreGameUIProducer_pollPingRefresh: poll ($pingRefreshPolls) should now be 0")
        }

        private fun dispatchPingRefresh() {
            check(pingRefresh?.isActive != true) {
                "duplicate ping refresh"
            }
            pingRefresh = rememberCoroutineScope!!.launch(lifetime) {
                if (pingRefreshPolls == 0) return@launch
                while (true) {
                    if (
                        lastPingRefreshStamp != -1L &&
                        SystemClock.elapsedRealtime() - lastPingRefreshStamp < 1000
                    ) {
                        delay(1000 - (SystemClock.elapsedRealtime() - lastPingRefreshStamp))
                    }
                    if (pingRefreshPolls > 0) {
                        val consumePoll = pingRefreshPolls
                        Log.d("LivePreGamePresenter.kt", "LivePreGameUIProducer_dispatchPingRefresh: recorded poll ($consumePoll) before fetching")
                        lastPingRefreshStamp = SystemClock.elapsedRealtime()
                        client!!.fetchPingMillis().await()
                            .onSuccess { pings ->
                                gamePodsPings.clear()
                                pings.forEach { entry ->
                                    gamePodsPings[entry.key] = entry.value
                                }
                                onNewPingData()
                            }
                            .onFailure {
                                gamePodsPings.clear()
                            }
                        Log.d("LivePreGamePresenter.kt", "LivePreGameUIProducer_dispatchPingRefresh: consuming poll ($consumePoll) from current poll ($pingRefreshPolls)")
                        pingRefreshPolls -= consumePoll
                    }
                    if (pingRefreshPolls == 0) break
                }
            }
        }
        private fun onNewPingData() {
            mutateState("onNewPingData") { state ->
                state.copy(
                    gamePodPing = gamePodsPings[state.gamePodId] ?: -1,
                )
            }
        }

        private fun autoRefreshScheduler(): Job {
            return rememberCoroutineScope!!.launch(lifetime) {
                while (isActive) {
                    // delay until at least 1 second from the last refresh
                    if (lastDataRefreshStamp != -1L) {
                        val ms = 1000 - (SystemClock.elapsedRealtime() - lastDataRefreshStamp)
                        Log.d("LivePreGamePresenter.kt", "autoRefreshScheduler delaying for ${ms}ms")
                        delay(ms)
                    }
                    onAutoRefresh()
                    runCatching { latestDataRefresh?.join() }
                }
            }
        }

        private fun expectMainThread(
            lazyMessage: () -> String
        ) {
            check(
                value = Looper.myLooper()?.let { it == Looper.getMainLooper() } == true,
                lazyMessage = lazyMessage
            )
        }

        private fun expectMainThread(
            actionName: String
        ) {
            check(
                value = Looper.myLooper()?.let { it == Looper.getMainLooper() } == true,
                lazyMessage = { "$actionName was not called in MainThread" }
            )
        }
    }
}