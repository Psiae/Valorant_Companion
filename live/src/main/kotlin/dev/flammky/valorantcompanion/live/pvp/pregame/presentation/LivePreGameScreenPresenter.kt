package dev.flammky.valorantcompanion.live.pvp.pregame.presentation

import android.os.SystemClock
import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.*
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.di.compose.inject
import dev.flammky.valorantcompanion.pvp.map.ValorantMapIdentity
import dev.flammky.valorantcompanion.pvp.mode.ValorantGameType
import dev.flammky.valorantcompanion.pvp.pregame.*
import dev.flammky.valorantcompanion.pvp.pregame.ex.PreGameMatchNotFoundException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.nanoseconds

@Composable
internal fun rememberLivePreGameScreenPresenter(
    authRepository: RiotAuthRepository = inject(),
    preGameService: PreGameService = inject(),
): LivePreGameScreenPresenter {

    return remember(authRepository, preGameService) {
        LivePreGameScreenPresenter(authRepository, preGameService)
    }
}

class LivePreGameScreenPresenter(
    private val authRepository: RiotAuthRepository,
    private val preGameService: PreGameService
) {

    @Composable
    fun present(): LivePreGameScreenState {
        val activeAccountState = remember(this) {
            mutableStateOf<AuthenticatedAccount?>(null)
        }
        val loaded = remember(this) {
            mutableStateOf<Boolean>(false)
        }
        DisposableEffect(
            this,
        ) {
            val listener = ActiveAccountListener { old, new ->
                loaded.value = true
                activeAccountState.value = new
            }
            authRepository.registerActiveAccountChangeListener(
                listener
            )
            onDispose {
                authRepository.unRegisterActiveAccountListener(listener)
            }
        }
        if (!loaded.value) {
            return LivePreGameScreenState.UNSET.copy(
                noOp = true,
                noOpMessage = "LOADING USER ..."
            )
        }
        return activeAccountState.value
            ?.let { account ->
                present(account.model.id)
            }
            ?: LivePreGameScreenState.UNSET.copy(
                noOp = true,
                noOpMessage = "USER NOT LOGGED IN"
            )
    }

    // we can use `key` instead
    @Composable
    fun present(user: String): LivePreGameScreenState {
        return rememberStateProducer(user = user).apply {
            SideEffect { produce() }
        }.readSnapshot()
    }

    @Composable
    private fun rememberStateProducer(
        user: String
    ): UIStateProducer {
        return remember(user) {
            check(user.isNotBlank()) {
                "Cannot Present without User"
            }
            UIStateProducer(user)
        }
    }


    inner class UIStateProducer (
        private val user: String
    ) : RememberObserver {
        private var _state = mutableStateOf<LivePreGameScreenState?>(null)

        private var remembered = false
        private var forgotten = false
        private var abandoned = false
        private var producing = false
        private var producer: Job? = null
        private var _coroutineScope: CoroutineScope? = null
        private val lifetime = SupervisorJob()
        private var _preGameClient: PreGameUserClient? = null

        private val coroutineScope get() = _coroutineScope!!
        private val preGameClient get() = _preGameClient!!

        private var currentMatchPollTS = -1L
        private var currentMatchDataPollTS = -1L

        private var lastActiveMatchID: String? = null

        private var pingStateProducer: Job? = null
        private var pingStateProducerCont: CompletableJob? = null
        private var pingPollTS = -1L
        private var pingResult: Map<String, Int> = emptyMap()

        private var stateGamePodID = ""

        @MainThread
        fun produce() {
            check(inMainLooper()) {
                "produce must be called on the MainThread, " +
                        "make sure this function is called within a side-effect block"
            }
            check(remembered) {
                "StateProducer must be remembered before calling produce, " +
                        "expected for compose runtime to invoke remember observer before side-effects"
            }
            check(!forgotten) {
                "StateProducer must not be forgotten before calling produce" +
                        "expected for compose runtime to not invoke side-effects when forgotten"
            }
            if (producing) return
            producing = true
            producer = coroutineScope.launch { produceState() }
        }

        private suspend fun produceState() {
            mutateState("produceState") { _ ->
                LivePreGameScreenState.UNSET.copy(user = user)
            }
            loop {
                val client = pollPlayerCurrentMatchIdForClient()
                runCatching { newMatch(client) }
                    .onFailure { ex ->
                        client.dispose()
                        throw ex
                    }
                    .onSuccess {
                        client.dispose()
                    }
            }
        }

        private suspend fun pollPlayerCurrentMatchIdForClient(): PreGameUserMatchClient {
            mutateState("pollCurrentMatchIdForClient") { state ->
                state.copy(
                    explicitLoading = true,
                    explicitLoadingMessage = "FINDING MATCH ...",
                )
            }
            val client = strictResultingLoop {
                if (currentMatchPollTS != -1L) {
                    delay(500 - (SystemClock.elapsedRealtime() - currentMatchPollTS))
                }
                currentMatchPollTS = SystemClock.elapsedRealtime()
                val result = preGameClient.fetchCurrentPreGameMatchId().await()
                onPollCurrentMatchForClientResult(result)
                    ?.let { client -> LOOP_BREAK(client) }
                    ?: LOOP_CONTINUE()
            }
            return client
        }

        private suspend fun onPollCurrentMatchForClientResult(
            result: PreGameFetchRequestResult<String>
        ): PreGameUserMatchClient? {
            result
                .onSuccess { id ->
                    if (id != lastActiveMatchID) {
                        lastActiveMatchID = id
                        return preGameClient.createMatchClient(id)
                    }
                }
                .onFailure { exception, errorCode ->
                    onFetchCurrentMatchFail(exception, errorCode)
                }
            return null
        }

        private suspend fun onFetchCurrentMatchFail(
            exception: Exception,
            errorCode: Int,
        ) {
            if (exception is PreGameMatchNotFoundException) {
                mutateState("fetchCurrentMatchFail_matchNotFound") { state ->
                    state.copy(
                        inMatch = false,
                        matchId = null,
                        mapName = null,
                        gameTypeName = null,
                        gamePodName = null,
                        gamePodPingMs = null,
                        countdown = null,
                        ally = null,
                    )
                }
                return
            }
            val cont = Job(lifetime)
            mutateState("fetchCurrentMatchFail_$errorCode") { state ->
                state.copy(
                    needUserRefresh = true,
                    needUserRefreshMessage = "UNABLE TO RETRIEVE USER CURRENT MATCH INFO ($errorCode)",
                    needUserRefreshRunnable = {
                        mutateState("fetchCurrentMatchFail_needUserRefreshRunnable") { state ->
                            state.copy(
                                needUserRefresh = false,
                                needUserRefreshMessage = null,
                                needUserRefreshRunnable = {}
                            )
                        }
                        cont.complete()
                    },
                    inMatch = false,
                    matchId = null,
                    mapName = null,
                    gameTypeName = null,
                    gamePodName = null,
                    gamePodPingMs = null,
                    countdown = null,
                    ally = null,
                )
            }
            cont.join()
        }

        private suspend fun newMatch(client: PreGameUserMatchClient) {
            var initialLoop = true
            var mnf = false
            var explicitLoading = true
            runCatching {
                strictLoop {
                    mutateState("onNewMatchFound") { state ->
                        state.copy(
                            explicitLoading = explicitLoading,
                            explicitLoadingMessage = run {
                                if (explicitLoading) {
                                    if (initialLoop) "MATCH FOUND, CHECKING DATA ..."
                                    else "LOADING DATA ..."
                                }
                                else state.explicitLoadingMessage
                            },
                            inMatch = true,
                            matchId = client.matchId,
                        )
                    }
                    initialLoop = false
                    if (currentMatchDataPollTS != -1L) {
                        delay(1000 - (SystemClock.elapsedRealtime() - currentMatchDataPollTS))
                    }
                    currentMatchDataPollTS = SystemClock.elapsedRealtime()
                    val result = client.fetchMatchInfoAsync().await()
                    result
                        .onSuccess { data ->
                            newMatchDataFromMatchPoll(client, data)
                            dispatchPollMatchPing()
                            explicitLoading = false
                        }
                        .onFailure { exception, errorCode ->
                            onFetchCurrentMatchDataFail(exception, errorCode.toString())
                            cancelPollMatchPing()
                            mnf = exception is PreGameMatchNotFoundException
                            explicitLoading = true
                        }
                    if (mnf) LOOP_BREAK() else LOOP_CONTINUE()
                }
            }.onFailure { ex ->
                cancelPollMatchPing()
                throw ex
            }
        }

        private suspend fun newMatchDataFromMatchPoll(
            client: PreGameUserMatchClient,
            data: PreGameMatchData
        ) = mutateState("fetchCurrentMatchDataSuccess") { state ->
            run sideEffect@ {
                stateGamePodID = data.gamePodId
            }
            val gameType = data.queueID
                ?.let { ValorantGameType.fromQueueID(it) }
                ?: ValorantGameType.fromProvisioningFlow(data.provisioningFlow)
            state.copy(
                inMatch = true,
                matchId = data.match_id,
                mapName = ValorantMapIdentity.ofID(data.mapID)?.display_name ?: "UNKNOWN_MAP_NAME",
                gameTypeName = gameType?.displayName,
                gamePodName = run {
                    // TODO: just hardcode the pod name
                    val hasPodNum = data.gamePodId.last().isDigit()
                    val segTake = if (hasPodNum) 2 else 1
                    var seg = 1
                    data.gamePodId.takeLastWhile { char ->
                        if (char == '-') seg++
                        seg <= segTake
                    }
                },
                gamePodPingMs = pingResult[data.gamePodId],
                countdown = data.phaseTimeRemainingNS.nanoseconds,
                allyKey = Any(),
                ally = run {
                    val team = data.allyTeam
                        ?: data.teams.find { team ->
                            team.players.any { player -> player.puuid == user }
                        }
                    team?.let { mapToUiPreGameTeam(team) }
                },
                selectAgent = { id -> client.selectAgentAsync(id) },
                lockAgent = { id -> client.lockAgentAsync(id) }
            )
        }

        private fun mapToUiPreGameTeam(
            domain: dev.flammky.valorantcompanion.pvp.pregame.PreGameTeam
        ): PreGameTeam {
            return PreGameTeam(
                teamID = domain.teamID,
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
                                dev.flammky.valorantcompanion.pvp.pregame.PreGamePlayerState.JOINED -> PreGamePlayerState.JOINED
                                is dev.flammky.valorantcompanion.pvp.pregame.PreGamePlayerState.ELSE -> PreGamePlayerState.ELSE
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

        private suspend fun onFetchCurrentMatchDataFail(exception: Exception, errorCode: String) {
            if (exception is PreGameMatchNotFoundException) {
                mutateState("fetchCurrentMatchFail_matchNotFound") { state ->
                    state.copy(
                        inMatch = false,
                        matchId = null,
                        explicitLoading = false,
                        explicitLoadingMessage = null,
                        mapName = null,
                        gameTypeName = null,
                        gamePodName = null,
                        gamePodPingMs = null,
                        ally = null,
                        allyKey = Any(),
                        selectAgent = {},
                        lockAgent = {}
                    )
                }
                return
            }
            val cont = Job(lifetime)
            mutateState("fetchCurrentMatchDataFail_$errorCode") { state ->
                state.copy(
                    needUserRefresh = true,
                    needUserRefreshMessage = "UNABLE TO RETRIEVE USER CURRENT MATCH INFO ($errorCode)",
                    needUserRefreshRunnable = {
                        mutateState("fetchCurrentMatchDataFail_needUserRefreshRunnable") { state ->
                            state.copy(
                                needUserRefresh = false,
                                needUserRefreshMessage = null,
                                needUserRefreshRunnable = {}
                            )
                        }
                        cont.complete()
                    },
                    inMatch = false,
                    matchId = null,
                    mapName = null,
                    gameTypeName = null,
                    gamePodName = null,
                    gamePodPingMs = null,
                    ally = null,
                    allyKey = Any(),
                    selectAgent = {},
                    lockAgent = {}
                )
            }
            cont.join()
        }

        @MainThread
        private fun dispatchPollMatchPing() {
            checkInMainLooper()
            if (pingStateProducer?.isActive == true) {
                pingStateProducerCont!!.complete()
                return
            }
            pingStateProducerCont = Job(lifetime)
            pingStateProducer = coroutineScope.launch { producePingState() }
        }

        @MainThread
        private fun cancelPollMatchPing() {
            checkInMainLooper()
            pingStateProducer?.cancel()
        }

        private suspend fun producePingState() {
            runCatching {
                loop {
                    if (pingPollTS != -1L) {
                        delay(500 - (SystemClock.elapsedRealtime() - pingPollTS))
                    }
                    pingPollTS = SystemClock.elapsedRealtime()
                    coroutineContext.ensureActive()
                    val def = preGameClient.fetchPingMillisAsync()
                    runCatching { def.await() }
                        .onFailure {
                            def.cancel()
                        }
                        .onSuccess { result ->
                            result.onSuccess(::onFetchPingSuccess).onFailure { ex ->
                                onFetchPingFailure(ex as Exception)
                            }
                        }
                    pingStateProducerCont!!.join()
                }
            }.onFailure { ex ->
                onFetchPingFailure(ex as Exception)
                throw ex
            }
        }

        private fun onFetchPingFailure(
            ex: Exception,
            /* errorCode: Int,*/
        ) {
            pingResult = emptyMap()
            mutateState("onFetchPingFailure") { state ->
                state.copy(
                    gamePodPingMs = null
                )
            }
        }

        private fun onFetchPingSuccess(
            data: Map<String, Int>
        ) {
            pingResult = data
            mutateState("onFetchPingSuccess") { state ->
                state.copy(
                    gamePodPingMs = pingResult[stateGamePodID],
                )
            }
        }

        @Composable
        @AnyThread
        @SnapshotRead
        fun readSnapshot(): LivePreGameScreenState {
            return stateValueOrUnset()
        }

        @SnapshotRead
        private fun stateValueOrUnset(): LivePreGameScreenState {
            return _state.value ?: LivePreGameScreenState.UNSET
        }

        override fun onAbandoned() {
            super.onAbandoned()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            abandoned = true
        }

        override fun onForgotten() {
            super.onForgotten()
            check(remembered)
            check(!forgotten)
            check(!abandoned)
            forgotten = true
            coroutineScope.cancel()
            preGameClient.dispose()
            lifetime.cancel()
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + lifetime)
            _preGameClient = preGameService.createUserClient(user)
        }

        private fun mutateState(
            action: String,
            mutate: (LivePreGameScreenState) -> LivePreGameScreenState
        ) {
            check(inMainLooper())
            val current = stateValueOrUnset()
            val new = mutate(current)
            Log.d("live.pregame.LivePreGameScreenPresenter.UIStateProducer", "mutateState($action), result=$new")
            _state.value = new
        }
    }
}