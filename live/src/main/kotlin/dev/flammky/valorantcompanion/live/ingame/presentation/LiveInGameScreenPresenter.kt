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
        private var inInitialRefreshSession = true

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
                val match = pollCurrentMatchForClient()
                onNewMatchFound(match)
            }
        }

        private fun onInitialProduce() {
            mutateState("onInitialProduce") { state ->
                state.copy(
                    user = user,
                    inMatch = null,
                    matchKey = null,
                    showLoading = true,
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
            mutateState("onUserInitiatedProduce") { state ->
                state.copy(
                    user = user,
                    inMatch = null,
                    matchKey = null,
                    showLoading = true,
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
                    return inGameClient.createMatchClient(id)
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
                )
            }
            pollMatchInfoUntilNotFound(client)
        }

        private suspend fun pollMatchInfoUntilNotFound(client: InGameUserMatchClient) {
            while (true) {
                delay(500 - (SystemClock.elapsedRealtime() - currentMatchDataPollTS))
                currentMatchDataPollTS = SystemClock.elapsedRealtime()
                val result = client.fetchMatchInfoAsync().await()
                var next = false
                result
                    .onSuccess { data ->
                        newMatchData(data)
                    }
                    .onFailure { exception, errorCode ->
                        val continuation = Job(lifetime)
                        onFetchCurrentMatchDataFail(exception, errorCode, continuation)
                        continuation.join()
                        next = exception !is InGameMatchNotFoundException
                    }
                if (!next) break
            }
        }

        private suspend fun onFetchCurrentMatchSuccess(

        ) {

        }

        private suspend fun onFetchCurrentMatchFail(
            exception: Exception,
            errorCode: Int,
        ) {
            check(this.pendingError?.isActive == false) {
                "duplicate pendingError"
            }
            if (exception is InGameMatchNotFoundException) {
                val cont = Job(lifetime)
                this.pendingError = cont
                mutateState("fetchCurrentMatchFail") { state ->
                    state.copy(
                        inMatch = false,
                        matchKey = null,
                        mapName = null,
                        gameTypeName = null,
                        gamePodName = null,
                        gamePodPingMs = null,
                        ally = InGameTeam.UNSET,
                        enemy = InGameTeam.UNSET,
                        errorMessage = null,
                    )
                }
                return
            }
        }

        private fun newMatchData(
            data: InGameMatchData
        ) = mutateState("fetchCurrentMatchDataSuccess") { state ->
            state.copy(
                inMatch = true,
                matchKey = data.matchID,
                mapName = ValorantMapIdentity.ofID(data.mapID)?.display_name ?: "UNKNOWN_MAP_NAME",
                gameTypeName = data.queueID?.let { ValorantGameType.fromQueueID(it) }?.displayName,
                gamePodName = toGamePodName(data.gamePodID),
                gamePodPingMs = state.gamePodPingMs
            )
        }

        private fun onFetchCurrentMatchDataFail(
            exception: Exception,
            errorCode: Int,
            continuation: CompletableJob
        ) {
            check(this.pendingError?.isActive == false) {
                "duplicate pendingError"
            }
            if (exception is InGameMatchNotFoundException) {
                continuation.complete()
                mutateState("fetchCurrentMatchFail") { state ->
                    state.copy(
                        inMatch = false,
                        matchKey = null,
                        mapName = null,
                        gameTypeName = null,
                        gamePodName = null,
                        gamePodPingMs = null,
                        ally = InGameTeam.UNSET,
                        enemy = InGameTeam.UNSET,
                        errorMessage = null,
                    )
                }
                return
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
    }
}