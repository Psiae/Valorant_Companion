package dev.flammky.valorantcompanion.live.ingame.presentation

import dev.flammky.valorantcompanion.base.UNSET

data class LiveInGameScreenState(
    val user: String,
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
    val ally: InGameTeam?,
    val enemy: InGameTeam?,
    val errorMessage: String?,
    val errorRefresh: () -> Unit,
): UNSET<LiveInGameScreenState> by Companion {

    companion object : UNSET<LiveInGameScreenState> {

        override val UNSET = LiveInGameScreenState(
            user = "",
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
            errorMessage = "",
            errorRefresh = {}
        )
    }
}
