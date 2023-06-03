package dev.flammky.valorantcompanion.pvp.pregame

import dev.flammky.valorantcompanion.pvp.TeamID
import kotlinx.collections.immutable.ImmutableList

data class PreGameTeam(
    val teamID: TeamID,
    val players: ImmutableList<PreGamePlayer>
)