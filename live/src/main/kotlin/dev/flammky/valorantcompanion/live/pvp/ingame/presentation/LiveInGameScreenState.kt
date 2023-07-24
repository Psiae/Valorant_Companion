package dev.flammky.valorantcompanion.live.pvp.ingame.presentation

import dev.flammky.valorantcompanion.base.UNSET

// data class for copy function convenience, use identity equality
data class LiveInGameScreenState(
    val user: String,
    val noOp: Boolean,
    // non-null if noOp is true, may be empty
    val noOpMessage: String?,
    val noOpError: Boolean,
    val needUserRefresh: Boolean,
    // non-null if needUserRefreshMessage is true, may be empty
    val needUserRefreshMessage: String?,
    // non-null if needUserRefreshMessage is true
    val needUserRefreshRunnable: (() -> Unit)?,
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
    val ally: dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam?,
    // val enemyKey: Any
    val enemy: dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam?,
): UNSET<LiveInGameScreenState> by Companion {

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    companion object : UNSET<LiveInGameScreenState> {

        override val UNSET = LiveInGameScreenState(
            user = "",
            noOp = false,
            noOpMessage = null,
            noOpError = false,
            needUserRefresh = false,
            needUserRefreshMessage = null,
            needUserRefreshRunnable = null,
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
