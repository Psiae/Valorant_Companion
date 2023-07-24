package dev.flammky.valorantcompanion.live.pvp.pregame.presentation

import dev.flammky.valorantcompanion.pvp.TeamID
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class PreGameTeam(
    val teamID: TeamID,
    val players: ImmutableList<PreGamePlayer>,
) {

    companion object {
        val UNSET = PreGameTeam(
            TeamID.RED,
            persistentListOf()
        )
    }
}
