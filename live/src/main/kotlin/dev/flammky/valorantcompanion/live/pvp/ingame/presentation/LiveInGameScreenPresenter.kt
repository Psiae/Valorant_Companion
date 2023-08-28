package dev.flammky.valorantcompanion.live.pvp.ingame.presentation

import android.os.SystemClock
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import dev.flammky.valorantcompanion.auth.riot.RiotAuthRepository
import dev.flammky.valorantcompanion.base.*
import dev.flammky.valorantcompanion.base.compose.RememberObserver
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.compose.state.SnapshotWrite
import dev.flammky.valorantcompanion.base.di.compose.inject
import dev.flammky.valorantcompanion.pvp.ingame.*
import dev.flammky.valorantcompanion.pvp.ingame.ex.InGameMatchNotFoundException
import dev.flammky.valorantcompanion.pvp.map.ValorantMapIdentity
import dev.flammky.valorantcompanion.pvp.mode.ValorantGameType
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

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
            return LiveInGameScreenState.UNSET.copy(
                noOp = true,
                noOpMessage = "LOADING USER ..."
            )
        }
        return activeAccountState.value
            ?.let { account -> present(account.model.id) }
            ?: LiveInGameScreenState.UNSET.copy(
                noOp = true,
                noOpMessage = "USER NOT LOGGED IN"
            )
    }


    @Composable
    fun present(
        user: String
    ): LiveInGameScreenState {
        return rememberStateProducer(
            user = user
        ).apply {
            SideEffect {
                produce()
            }
        }.readSnapshot()
    }

    @Composable
    private fun rememberStateProducer(
        user: String
    ): StateProducer {
        return remember(this, user) {
            check(user.isNotBlank()) {
                "Cannot Present without User"
            }
            StateProducer(user)
        }
    }


    private inner class StateProducer(private val user: String) :
        RememberObserver {

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

        private var pingStateProducer: Job? = null
        private var pingStateProducerCont: CompletableJob? = null
        private var pingPollTS = -1L
        private var pingResult: Map<String, Int> = emptyMap()

        private var stateGamePodID = ""

        @SnapshotWrite
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
            onInitialProduce()
            strictLoop {
                val match = pollCurrentMatchForClient()
                runCatching { onNewMatchFound(match) }
                    .onFailure { ex ->
                        match.dispose()
                        throw ex
                    }
                    .onSuccess {
                        match.dispose()
                    }
                LOOP_CONTINUE()
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
                )
            }
        }

        private suspend fun pollCurrentMatchForClient(): InGameUserMatchClient {
            mutateState("pollCurrentMatchForClient") { state ->
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
                val result = inGameClient.fetchUserCurrentMatchIDAsync().await()
                onPollCurrentMatchForClientResult(result)
                    ?.let { client -> LOOP_BREAK(client) }
                    ?: LOOP_CONTINUE()
            }
            return client
        }

        private suspend fun onPollCurrentMatchForClientResult(
            result: InGameFetchRequestResult<String>
        ): InGameUserMatchClient? {
            result
                .onSuccess { id ->
                    val client = inGameClient.createMatchClient(id)
                    val def = client.fetchMatchInfoAsync()
                    runCatching {
                        def.await()
                            .onSuccess { info ->
                                return client.takeIf { !info.matchOver }
                            }
                            .onFailure { ex, er ->
                                onFetchCurrentMatchFail(ex, er)
                            }
                    }.onFailure { ex -> def.cancel() ; throw ex }
                    client.dispose()
                }
                .onFailure { exception, errorCode ->
                    onFetchCurrentMatchFail(exception, errorCode)
                }
            return null
        }


        private suspend fun onNewMatchFound(client: InGameUserMatchClient) {
            var initialLoop = true
            var mnf = false
            var matchOver = false
            var explicitLoading = true
            strictLoop {
                mutateState("onNewMatchFound") { state ->
                    state.copy(
                        inMatch = true,
                        matchKey = client.matchID,
                        pollingForMatch = false,
                        userRefreshing = false,
                        explicitLoading = explicitLoading,
                        explicitLoadingMessage = run {
                            if (explicitLoading) {
                                if (initialLoop) "MATCH FOUND, CHECKING DATA ..."
                                else "LOADING DATA ..."
                            }
                            else state.explicitLoadingMessage
                        }
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
                        newMatchDataFromMatchPoll(data)
                        dispatchPollMatchPing()
                        matchOver = data.matchOver
                        explicitLoading = false
                    }
                    .onFailure { exception, errorCode ->
                        onFetchCurrentMatchDataFail(exception, errorCode)
                        cancelPollMatchPing()
                        mnf = exception is InGameMatchNotFoundException
                        explicitLoading = true
                    }
                if (mnf || matchOver) LOOP_BREAK() else LOOP_CONTINUE()
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
                    matchKey = null,
                    pollingForMatch = false,
                    userRefreshing = false,
                    mapName = null,
                    gameTypeName = null,
                    gamePodName = null,
                    gamePodPingMs = null,
                    ally = null,
                    enemy = null,
                )
            }
            cont.join()
        }

        private fun newMatchDataFromMatchPoll(
            data: InGameMatchInfo
        ) = mutateState("fetchCurrentMatchDataSuccess") { state ->
            // TODO: fallback
            if (data.matchOver) {
                run sideEffect@ {
                    stateGamePodID = ""
                }
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
                run sideEffect@ {
                    stateGamePodID = data.gamePodID
                }
                val gameType = data.queueID
                    ?.let { ValorantGameType.fromQueueID(it) }
                    ?: ValorantGameType.fromProvisioningFlow(data.provisioningFlow)
                val user = data.players.find { it.puuid == user }
                val ally = parseAllyMembers(gameType, data.players, user)
                val enemy = parseEnemyMembers(gameType, data.players, user)
                state.copy(
                    inMatch = true,
                    matchKey = data.matchID,
                    explicitLoading = false,
                    explicitLoadingMessage = null,
                    mapName = ValorantMapIdentity.ofID(data.mapID)?.display_name ?: "UNKNOWN_MAP_NAME",
                    gameTypeName = gameType?.displayName,
                    gamePodName = toGamePodName(data.gamePodID),
                    gamePodPingMs = pingResult[data.gamePodID],
                    allyMembersProvided = ally.members.isNotEmpty(),
                    ally = ally,
                    enemyMembersProvided = enemy.members.isNotEmpty(),
                    enemy = enemy
                )
            }
        }

        private fun parseAllyMembers(
            gameType: ValorantGameType?,
            players: List<InGamePlayer>,
            user: InGamePlayer? = players.find { it.puuid == this.user }
        ): dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam {
            return dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam(
                id = user?.teamID,
                members = if (gameType is ValorantGameType.DEATHMATCH) {
                    listOfNotNull(user)
                } else {
                    players.filter { it.teamID == user?.teamID }
                }.mapTo(persistentListOf<TeamMember>().builder()) {
                    TeamMember(
                        puuid = it.puuid,
                        agentID = it.character_id,
                        playerCardID = it.playerIdentity.playerCardId,
                        accountLevel = it.playerIdentity.accountLevel,
                        incognito = it.playerIdentity.incognito
                    )
                }.build()
            )
        }

        private fun parseEnemyMembers(
            gameType: ValorantGameType?,
            players: List<InGamePlayer>,
            user: InGamePlayer? = players.find { it.puuid == this.user }
        ): dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam {
            return dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam(
                id = user?.teamID,
                members = if (gameType is ValorantGameType.DEATHMATCH) {
                    players.filter { it != user }
                } else {
                    players.filter { it.teamID != user?.teamID }
                }.mapTo(persistentListOf<TeamMember>().builder()) {
                    TeamMember(
                        puuid = it.puuid,
                        agentID = it.character_id,
                        playerCardID = it.playerIdentity.playerCardId,
                        accountLevel = it.playerIdentity.accountLevel,
                        incognito = it.playerIdentity.incognito
                    )
                }.build()
            )
        }

        private suspend fun onFetchCurrentMatchDataFail(
            exception: Exception,
            errorCode: Int,
        ) {
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
                    matchKey = null,
                    pollingForMatch = false,
                    userRefreshing = false,
                    mapName = null,
                    gameTypeName = null,
                    gamePodName = null,
                    gamePodPingMs = null,
                    ally = null,
                    enemy = null,
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
                    val def = inGameClient.fetchPingMillisAsync()
                    runCatching { def.await() }
                        .onFailure {
                            def.cancel()
                        }
                        .onSuccess { result ->
                            result.onSuccess(::onFetchPingSuccess).onFailure { ex, er ->
                                onFetchPingFailure(ex)
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
                    gamePodPingMs = -1
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
            lifetime.cancel()
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