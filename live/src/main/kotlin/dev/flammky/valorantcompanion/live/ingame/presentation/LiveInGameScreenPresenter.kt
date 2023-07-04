package dev.flammky.valorantcompanion.live.ingame.presentation

import android.os.SystemClock
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.compose.BaseRememberObserver
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.compose.state.SnapshotWrite
import dev.flammky.valorantcompanion.base.di.compose.inject
import dev.flammky.valorantcompanion.base.inMainLooper
import dev.flammky.valorantcompanion.pvp.ingame.*
import dev.flammky.valorantcompanion.pvp.ingame.ex.InGameMatchNotFoundException
import dev.flammky.valorantcompanion.pvp.map.ValorantMapIdentity
import dev.flammky.valorantcompanion.pvp.mode.ValorantGameType
import kotlinx.coroutines.*

@Composable
internal fun rememberLiveInGameScreenPresenter(
    inGameService: InGameService = inject(),
    authRepository: RiotAuthRepository = inject()
): LiveInGameScreenPresenter {
    return remember(inGameService, authRepository) {
        LiveInGameScreenPresenter(inGameService, authRepository)
    }
}

internal class LiveInGameScreenPresenter(
    private val inGameService: InGameService,
    private val authRepository: RiotAuthRepository
) {

    @Composable
    fun present(): LiveInGameScreenState {
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
        return present(user = activeAccountState.value?.model?.id ?: "")
    }


    @Composable
    fun present(
        user: String
    ): LiveInGameScreenState {

        if (user.isEmpty()) {
            return LiveInGameScreenState.UNSET
        }

        val producer = remember(user) {
            StateProducer(user)
        }

        SideEffect {
            producer.produce()
        }

        return producer.readSnapshot()
    }


    private inner class StateProducer(private val user: String) : BaseRememberObserver {

        private val _state = mutableStateOf<LiveInGameScreenState?>(null)
        private var _coroutineScope: CoroutineScope? = null
        private var _inGameClient: InGameUserClient? = null
        private val lifetime = SupervisorJob()
        private var remembered = false
        private var abandoned = false
        private var forgotten = false
        private var producing = false
        private var producer: Job? = null

        private val coroutineScope get() = _coroutineScope!!
        private val inGameClient get() =  _inGameClient!!

        private var pendingError: CompletableJob? = null

        private var currentMatchPollTS = 0L
        private var currentMatchDataPollTS = 0L

        private var inUserRefreshSession = false
        private var inInitialRefreshSession = false
        private val inExplicitRefreshSession get() = inUserRefreshSession || inInitialRefreshSession
        private var lastActiveMatchID: String? = null

        @SnapshotWrite
        @MainThread
        fun produce() {
            check(inMainLooper())
            check(remembered) {
                "StateProducer must be remembered before calling produce, " +
                        "expected for compose runtime to invoke remember observer before side-effects"
            }
            if (producing) return
            producing = true
            inInitialRefreshSession = true
            producer = coroutineScope.launch { produceState() }
        }

        private suspend fun produceState() {
            if (inInitialRefreshSession) {
                onInitialProduce()
            } else if (inUserRefreshSession) {
                onUserInitiatedProduce()
            }
            while (true) {
                val match = run onPollCurrentMatchForClient@ {
                    mutateState("produceState_onPollCurrentMatchForClient") { state ->
                        state.copy(
                            explicitLoading = true,
                            explicitLoadingMessage = "FINDING MATCH...",
                        )
                    }
                    pollCurrentMatchForClient()
                }
                onNewMatchFound(match)
            }
        }

        private fun onInitialProduce() {
            mutateState("onInitialProduce") { state ->
                state.copy(
                    user = user,
                    inMatch = null,
                    matchKey = null,
                    pollingForMatch = true,
                    userRefreshing = false,
                    mapName = null,
                    gameTypeName = null,
                    gamePodName = null,
                    gamePodPingMs = null,
                    ally = null,
                    enemy = null,
                    errorMessage = null,
                    errorRefresh = {}
                )
            }
        }

        private fun onUserInitiatedProduce() {
            TODO("Not Yet Implemented")
            mutateState("onUserInitiatedProduce") { state ->
                state.copy(
                    user = user,
                    inMatch = null,
                    matchKey = null,
                    pollingForMatch = true,
                    mapName = null,
                    gameTypeName = null,
                    gamePodName = null,
                    gamePodPingMs = null,
                    ally = null,
                    enemy = null,
                    errorMessage = null,
                    errorRefresh = {}
                )
            }
        }

        private suspend fun pollCurrentMatchForClient(): InGameUserMatchClient {
            while (true) {
                delay(500 - (SystemClock.elapsedRealtime() - currentMatchPollTS))
                currentMatchPollTS = SystemClock.elapsedRealtime()
                val result = inGameClient.fetchUserCurrentMatchIDAsync().await()
                onPollCurrentMatchForClientResult(result)?.let { client -> return client }
            }
        }

        private suspend fun onPollCurrentMatchForClientResult(
            result: InGameFetchRequestResult<String>
        ): InGameUserMatchClient? {
            result
                .onSuccess { id ->
                    if (id != lastActiveMatchID) {
                        lastActiveMatchID = id
                        return inGameClient.createMatchClient(id)
                    }
                }
                .onFailure { exception, errorCode ->
                    onFetchCurrentMatchFail(exception, errorCode)
                }
            return null
        }


        private suspend fun onNewMatchFound(client: InGameUserMatchClient) {
            mutateState("onMatchFound") { state ->
                state.copy(
                    inMatch = true,
                    matchKey = client.matchID,
                    pollingForMatch = false,
                    userRefreshing = false,
                    explicitLoadingMessage = run {
                        if (inExplicitRefreshSession) "MATCH FOUND, LOADING DATA..."
                        else state.explicitLoadingMessage
                    }
                )
            }
            pollMatchInfoUntilNotFound(client)
        }

        private suspend fun pollMatchInfoUntilNotFound(client: InGameUserMatchClient) {
            while (true) {
                delay(1000 - (SystemClock.elapsedRealtime() - currentMatchDataPollTS))
                currentMatchDataPollTS = SystemClock.elapsedRealtime()
                val result = client.fetchMatchInfoAsync().await()
                var mnf = false
                var matchOver = false
                result
                    .onSuccess { data ->
                        newMatchDataFromMatchPoll(data)
                        matchOver = data.matchOver
                    }
                    .onFailure { exception, errorCode ->
                        onFetchCurrentMatchDataFail(exception, errorCode)
                        mnf = exception is InGameMatchNotFoundException
                    }
                if (mnf || matchOver) break
            }
        }

        private suspend fun onFetchCurrentMatchFail(
            exception: Exception,
            errorCode: Int,
        ) {
            check(this.pendingError?.isActive != true) {
                "duplicate pendingError"
            }
            if (exception is InGameMatchNotFoundException) {
                mutateState("fetchCurrentMatchFail_matchNotFound") { state ->
                    state.copy(
                        inMatch = false,
                        matchKey = null,
                        pollingForMatch = false,
                        userRefreshing = false,
                        mapName = null,
                        gameTypeName = null,
                        gamePodName = null,
                        gamePodPingMs = null,
                        allyMembersProvided = false,
                        ally = null,
                        enemyMembersProvided = false,
                        enemy = null,
                    )
                }
                return
            }
            val cont = Job(lifetime)
            this.pendingError = cont
            mutateState("fetchCurrentMatchFail_$errorCode") { state ->
                state.copy(
                    inMatch = false,
                    matchKey = null,
                    pollingForMatch = false,
                    userRefreshing = false,
                    mapName = null,
                    gameTypeName = null,
                    gamePodName = null,
                    gamePodPingMs = null,
                    ally = null,
                    enemy = null,
                    errorMessage = "unexpected error occurred ($errorCode)"
                )
            }
            cont.join()
        }

        private fun newMatchDataFromMatchPoll(
            data: InGameMatchInfo
        ) = mutateState("fetchCurrentMatchDataSuccess") { state ->
            // TODO: fallback
            if (data.matchOver) {
                state.copy(
                    inMatch = true,
                    matchKey = data.matchID,
                    explicitLoading = true,
                    explicitLoadingMessage = "MATCH ENDED",
                    mapName = null,
                    gameTypeName = null,
                    gamePodName = null,
                    gamePodPingMs = null,
                    allyMembersProvided = false,
                    ally = null,
                    enemyMembersProvided = false,
                    enemy = null
                )
            } else {
                val user = data.players.find { it.puuid == user }
                val ally = InGameTeam(
                    id = user?.teamID,
                    members = data.players
                        .filter { it.teamID == user?.teamID }
                        .map {
                            TeamMember(
                                puuid = it.puuid,
                                agentID = it.character_id,
                                playerCardID = it.playerIdentity.playerCardId,
                                accountLevel = it.playerIdentity.accountLevel,
                                incognito = it.playerIdentity.incognito)
                        }
                )
                val enemy = InGameTeam(
                    id = data.players.find { it.teamID != user?.teamID }?.teamID,
                    members = data.players
                        .run {
                            if (user != null) filter { it.teamID != user.teamID } else this
                        }
                        .map {
                            TeamMember(
                                puuid = it.puuid,
                                agentID = it.character_id,
                                playerCardID = it.playerIdentity.playerCardId,
                                accountLevel = it.playerIdentity.accountLevel,
                                incognito = it.playerIdentity.incognito)
                        }
                )
                state.copy(
                    inMatch = true,
                    matchKey = data.matchID,
                    explicitLoading = false,
                    explicitLoadingMessage = null,
                    mapName = ValorantMapIdentity.ofID(data.mapID)?.display_name ?: "UNKNOWN_MAP_NAME",
                    gameTypeName = data.queueID
                        ?.let { ValorantGameType.fromQueueID(it) }?.displayName
                        ?: ValorantGameType.fromProvisioningFlow(data.provisioningFlow)?.displayName,
                    gamePodName = toGamePodName(data.gamePodID),
                    gamePodPingMs = state.gamePodPingMs,
                    allyMembersProvided = ally.members.isNotEmpty(),
                    ally = ally,
                    enemyMembersProvided = enemy.members.isNotEmpty(),
                    enemy = enemy
                )
            }
        }

        private suspend fun onFetchCurrentMatchDataFail(
            exception: Exception,
            errorCode: Int,
        ) {
            check(this.pendingError?.isActive != true) {
                "duplicate pendingError"
            }
            if (exception is InGameMatchNotFoundException) {
                mutateState("fetchCurrentMatchFail_matchNotFound") { state ->
                    state.copy(
                        inMatch = false,
                        matchKey = null,
                        explicitLoading = false,
                        explicitLoadingMessage = null,
                        mapName = null,
                        gameTypeName = null,
                        gamePodName = null,
                        gamePodPingMs = null,
                        allyMembersProvided = false,
                        enemyMembersProvided = false,
                        ally = null,
                        enemy = null,
                        errorMessage = null,
                    )
                }
                return
            }
            mutateState("fetchCurrentMatchDataFail_$errorCode") { state ->
                state.copy(
                    inMatch = false,
                    matchKey = null,
                    explicitLoading = false,
                    explicitLoadingMessage = null,
                    pollingForMatch = false,
                    userRefreshing = false,
                    mapName = null,
                    gameTypeName = null,
                    gamePodName = null,
                    gamePodPingMs = null,
                    allyMembersProvided = false,
                    enemyMembersProvided = false,
                    ally = null,
                    enemy = null,
                    errorMessage = "unexpected error occurred ($errorCode)"
                )
            }
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
            _coroutineScope?.cancel()
            _inGameClient?.dispose()
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            _inGameClient = inGameService.createUserClient(user)
        }

        @SnapshotRead
        fun readSnapshot(): LiveInGameScreenState {
            return stateValueOrUnset()
        }

        private fun stateValueOrUnset(): LiveInGameScreenState {
           return _state.value ?: LiveInGameScreenState.UNSET
        }

        private fun mutateState(
            action: String,
            mutate: (LiveInGameScreenState) -> LiveInGameScreenState
        ) {
            check(inMainLooper())
            _state.value = mutate(stateValueOrUnset())
        }

        // TODO: hard-code known id, parse if not known
        private fun toGamePodName(id: String): String {
            val hasPodNum = id.last().isDigit()
            val segTake = if (hasPodNum) 2 else 1
            var seg = 1
            return id.takeLastWhile { char ->
                if (char == '-') seg++
                seg <= segTake
            }
        }

        private fun stateErrorMessage(): String? {
            return null
        }

        private fun shouldShowLoading(): Boolean {
            return false
        }
    }
}