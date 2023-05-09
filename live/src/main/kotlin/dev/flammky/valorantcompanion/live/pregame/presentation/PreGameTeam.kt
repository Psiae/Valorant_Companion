package dev.flammky.valorantcompanion.live.pregame.presentation

import kotlinx.collections.immutable.ImmutableList

data class PreGameTeam(
    val teamID: TeamID,
    val players: ImmutableList<PreGamePlayer>,
)

enum class TeamID {
    RED,
    BLUE
}
