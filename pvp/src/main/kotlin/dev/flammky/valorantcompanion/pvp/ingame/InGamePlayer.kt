package dev.flammky.valorantcompanion.pvp.ingame

import dev.flammky.valorantcompanion.pvp.TeamID

data class InGamePlayer(
    val puuid: String,
    val teamID: TeamID,
    val character_id: String,
    val playerIdentity: InGamePlayerIdentity,
)
