package dev.flammky.valorantcompanion.live.ingame.presentation

data class TeamMember(
    val puuid: String,
    val agentID: String,
    val playerCardID: String,
    val accountLevel: Int,
    val incognito: Boolean
)
