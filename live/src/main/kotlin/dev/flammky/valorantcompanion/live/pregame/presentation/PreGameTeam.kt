package dev.flammky.valorantcompanion.live.pregame.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class PreGameTeam(
    val teamID: TeamID,
    val players: ImmutableList<PreGamePlayer>,
) {

    companion object {
        val UNSET by lazy {
            PreGameTeam(TeamID.Custom(""), persistentListOf())
        }
    }
}

sealed interface TeamID {
    object RED : TeamID

    object BLUE : TeamID

    data class Custom(val name: String): TeamID

    companion object {
        fun parse(name: String) = when (name.lowercase()) {
            "red" -> TeamID.RED
            "blue" -> TeamID.BLUE
            else -> Custom(name)
        }
    }
}
