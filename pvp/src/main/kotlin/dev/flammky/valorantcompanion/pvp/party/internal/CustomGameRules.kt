package dev.flammky.valorantcompanion.pvp.party.internal

data class CustomGameRules(
    val allowGameModifiers: String?,
    val isOverTimeWinByTwo: String?,
    val playOutAllRounds: String?,
    val skipMatchHistory: String?,
    val tournamentMode: String?
) {
}