package dev.flammky.valorantcompanion.live.ingame.presentation

import dev.flammky.valorantcompanion.base.UNSET

data class LiveInGameScreenState(
    val user: String,
    val inMatch: Boolean?,
    val matchKey: Any?,
    val showLoading: Boolean,
    val mapName: String?,
    val gameTypeName: String?,
    val gamePodName: String?,
    val gamePodPingMs: Int?,
    val ally: InGameTeam?,
    val enemy: InGameTeam?,
    val errorMessage: String?,
    val errorRefresh: () -> Unit,
): UNSET<LiveInGameScreenState> by Companion {

    companion object : UNSET<LiveInGameScreenState> {

        override val UNSET = LiveInGameScreenState(
            "",
            false,
            null,
            false,
            "",
            "",
            "",
            -1,
            null,
            null,
            "",
            {}
        )
    }
}
