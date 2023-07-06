package dev.flammky.valorantcompanion.live.ingame.presentation

import dev.flammky.valorantcompanion.base.UNSET

data class LiveInGameScreenState(
    val user: String,
    val noOp: Boolean,
    // non-null if noOp is true, may be empty
    val noOpMessage: String?,
    val noOpError: Boolean,
    val needUserRefresh: Boolean,
    // non-null if needUserRefreshMessage is true, may be empty
    val needUserRefreshMessage: String?,
    val needUserRefreshRunnable: () -> Unit,
    val inMatch: Boolean?,
    val matchKey: Any?,
    val pollingForMatch: Boolean,
    val userRefreshing: Boolean,
    val explicitLoading: Boolean,
    val explicitLoadingMessage: String?,
    val mapName: String?,
    val gameTypeName: String?,
    val gamePodName: String?,
    val gamePodPingMs: Int?,
    val allyMembersProvided: Boolean,
    val enemyMembersProvided: Boolean,
    // val allyKey: Any
    val ally: InGameTeam?,
    // val enemyKey: Any
    val enemy: InGameTeam?,
): UNSET<LiveInGameScreenState> by Companion {

    companion object : UNSET<LiveInGameScreenState> {

        override val UNSET = LiveInGameScreenState(
            user = "",
            noOp = false,
            noOpMessage = null,
            noOpError = false,
            needUserRefresh = false,
            needUserRefreshMessage = null,
            needUserRefreshRunnable = {},
            inMatch = false,
            matchKey = null,
            pollingForMatch = false,
            userRefreshing = false,
            explicitLoading = false,
            explicitLoadingMessage = "",
            mapName = "",
            gameTypeName = "",
            gamePodName = "",
            gamePodPingMs = -1,
            allyMembersProvided = false,
            enemyMembersProvided = false,
            ally = null,
            enemy = null,
        )
    }
}
