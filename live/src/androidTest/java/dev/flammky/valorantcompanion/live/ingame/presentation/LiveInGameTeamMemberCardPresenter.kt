package dev.flammky.valorantcompanion.live.ingame.presentation

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.*
import dev.flammky.valorantcompanion.assets.ValorantAssetsLoaderClient
import dev.flammky.valorantcompanion.assets.ValorantAssetsService
import dev.flammky.valorantcompanion.base.compose.BaseRememberObserver
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead
import dev.flammky.valorantcompanion.base.di.compose.inject
import dev.flammky.valorantcompanion.base.inMainLooper
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import dev.flammky.valorantcompanion.pvp.mmr.ValorantMMRService
import dev.flammky.valorantcompanion.pvp.mmr.MMRUserClient
import dev.flammky.valorantcompanion.pvp.player.GetPlayerNameRequest
import dev.flammky.valorantcompanion.pvp.player.ValorantNameService
import dev.flammky.valorantcompanion.pvp.season.ValorantSeasons
import kotlinx.coroutines.*

@Composable
internal fun rememberLiveInGameTeamMemberCardStatePresenter(
    assetsService: ValorantAssetsService = inject(),
    nameService: ValorantNameService = inject(),
    mmrService: ValorantMMRService = inject()
): LiveInGameTeamMemberCardPresenter {
    return remember(assetsService, nameService, mmrService) {
        RealLiveInGameTeamMemberPresenter(assetsService, nameService, mmrService)
    }
}

internal interface LiveInGameTeamMemberCardStatePresenter {


    @Composable
    fun present(
        matchKey: Any,
        user: String,
        id: String,
        playerAgentID: String,
        playerCardID: String,
        accountLevel: Int,
        // ignored
        incognito: Boolean
    ): LiveInGameTeamMemberCardState
}

private class RealLiveInGameTeamMemberPresenter(
    private val assetsService: ValorantAssetsService,
    private val nameService: ValorantNameService,
    private val mmrService: ValorantMMRService
) : LiveInGameTeamMemberCardPresenter {

    @Composable
    override fun present(
        matchKey: Any,
        user: String,
        id: String,
        playerAgentID: String,
        playerCardID: String,
        accountLevel: Int,
        incognito: Boolean
    ): LiveInGameTeamMemberCardState {
        val producer = remember(user, matchKey) { StateProducer(user) }

        SideEffect {
            producer.produceParams(id, playerAgentID, playerCardID, accountLevel, incognito)
        }

        return producer.readSnapshot()
    }


    private inner class StateProducer(
        private val user: String,
    ) : BaseRememberObserver {

        private val _state = mutableStateOf<LiveInGameTeamMemberCardState?>(null, neverEqualPolicy())

        private var remembered = false
        private var forgotten = false
        private var abandoned = false

        private var _coroutineScope: CoroutineScope? = null
        private val coroutineScope get() = _coroutineScope!!

        private var _assetLoader: ValorantAssetsLoaderClient? = null
        private val assetLoader get() = _assetLoader!!

        private var _mmrClient: MMRUserClient? = null
        private val mmrClient get() = _mmrClient!!

        private var playerIdSupervisor: Job? = null

        private var isUser: Boolean? = null
        private var playerId: String? = null
        private var playerAgentID: String? = null
        private var playerCardID: String? = null
        private var accountLevel: Int? = null
        private var incognito: Boolean? = null

        @SnapshotRead
        fun readSnapshot(): LiveInGameTeamMemberCardState = stateValueOrUnset()

        @MainThread
        fun produceParams(
            id: String,
            playerAgentID: String,
            playerCardID: String,
            accountLevel: Int,
            incognito: Boolean
        ) {
            check(inMainLooper())
            check(remembered)
            if (id != this.playerId) {
                newPlayerID(id)
            }
            playerDataParam(playerAgentID, playerCardID, accountLevel, incognito)
        }

        private fun newPlayerID(
            id: String,
        ) {
            playerIdSupervisor?.cancel()

            this.playerId = id

            mutateState("newPlayerID") { state ->
                state.copy(
                    isUser = user == id,
                    username = state.UNSET.username,
                    tagline = state.UNSET.tagline,
                    competitiveTierIcon = state.UNSET.competitiveTierIcon,
                    competitiveTierIconKey = state.UNSET.competitiveTierIconKey
                )
            }

            playerIdSupervisor = SupervisorJob()
            newPlayerIdSideEffect()
        }

        private fun playerDataParam(
            playerAgentID: String,
            playerCardID: String,
            accountLevel: Int,
            incognito: Boolean
        ) {
            if (this.playerAgentID != playerAgentID) {
                newPlayerAgent(playerAgentID)
            }
            if (this.playerCardID != playerCardID) {
                // TODO
            }
            if (this.accountLevel != accountLevel) {
                // TODO
            }
            if (this.incognito != incognito) {
                // TODO
            }
        }

        private fun newPlayerAgent(
            id: String
        ) {
            this.playerAgentID = id
            val identity = ValorantAgentIdentity.ofID(id)
            val icon = assetLoader.loadMemoryCachedAgentIcon(id)
            val roleIcon = identity?.role?.let { it -> assetLoader.loadMemoryCachedRoleIcon(it.uuid) }
            mutateState("newPlayerAgent") { state ->
                state.copy(
                    agentName = identity?.displayName ?: state.UNSET.agentName,
                    agentIcon = icon ?: state.UNSET.agentIcon,
                    agentIconKey = icon?.let { Any() } ?: state.UNSET.agentIconKey,
                    roleName = identity?.role?.displayName ?: state.UNSET.roleName,
                    roleIcon = roleIcon ?: state.UNSET.roleIcon,
                    roleIconKey = roleIcon?.let { Any() } ?: state.UNSET.roleIconKey
                )
            }
        }

        override fun onAbandoned() {
            super.onAbandoned()
            check(!remembered)
            check(!abandoned)
            check(!forgotten)
            abandoned = true
        }

        override fun onForgotten() {
            super.onAbandoned()
            check(remembered)
            check(!abandoned)
            check(!forgotten)
            forgotten = true
            coroutineScope.cancel()
            _assetLoader?.dispose()
            _mmrClient = mmrService.createUserClient(user)
        }

        override fun onRemembered() {
            super.onRemembered()
            check(!remembered)
            check(!forgotten)
            check(!abandoned)
            remembered = true
            _coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            _assetLoader = assetsService.createLoaderClient()
            _mmrClient = mmrService.createUserClient(user)
        }

        @SnapshotRead
        private fun stateValueOrUnset(): LiveInGameTeamMemberCardState {
            return _state.value ?: LiveInGameTeamMemberCardState.UNSET
        }

        @MainThread
        private fun mutateState(
            actionName: String,
            mutate: (state: LiveInGameTeamMemberCardState) -> LiveInGameTeamMemberCardState
        ) {
            Log.d("LiveInGameTeamMemberCardStatePresenter", "mutateState($actionName)")
            check(inMainLooper())
            _state.value = mutate(stateValueOrUnset())
        }

        private fun newPlayerIdSideEffect() {
            val playerId = this.playerId!!

            coroutineScope.launch(playerIdSupervisor!!) {
                ensureActive()
                val nameResult = run {
                    val def = nameService.getPlayerNameAsync(
                        GetPlayerNameRequest(shard = null, signedInUserPUUID = user, listOf(playerId))
                    )
                    runCatching { def.await() }.onFailure { def.cancel() }.getOrThrow()
                }
                mutateState("newPlayerIdSideEffect_nameResult") { state ->
                    val identity = nameResult[playerId]?.getOrNull()
                    state.copy(
                        username = identity?.gameName,
                        tagline = identity?.tagLine
                    )
                }
            }
            coroutineScope.launch(playerIdSupervisor!!) {
                ensureActive()
                val mmrResult = run {
                    val def = mmrClient.fetchSeasonalMMR(ValorantSeasons.ACTIVE_STAGED.act.id, playerId)
                    runCatching { def.await() }.onFailure { def.cancel() }.getOrThrow()
                }.getOrElse { ex ->
                    mutateState("newPlayerIdSideEffect_mmrResult_fail") { state ->
                        state.copy(
                            competitiveTierIcon = state.UNSET.competitiveTierIcon,
                            competitiveTierIconKey = state.UNSET.competitiveTierIconKey
                        )
                    }
                    return@launch
                }
                val iconResult = run {
                    val def = assetLoader.loadCompetitiveRankIconAsync(mmrResult.competitiveRank)
                    runCatching { def.await() }.onFailure { def.cancel() }.getOrThrow()
                }.getOrElse {
                    mutateState("newPlayerIdSideEffect_iconResult_fail") { state ->
                        state.copy(
                            competitiveTierIcon = state.UNSET.competitiveTierIcon,
                            competitiveTierIconKey = state.UNSET.competitiveTierIconKey
                        )
                    }
                    return@launch
                }
                mutateState("newPlayerIdSideEffect_iconResult") { state ->
                    state.copy(
                        competitiveTierIcon = iconResult,
                        competitiveTierIconKey = Any()
                    )
                }
            }
        }
    }
}