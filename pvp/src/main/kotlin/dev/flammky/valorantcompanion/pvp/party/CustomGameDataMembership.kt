package dev.flammky.valorantcompanion.pvp.party

data class CustomGameDataMembership(
    val teamOne: List<String>?,
    val teamTwo: List<String>?,
    val teamSpectate: List<String>?,
    val teamOneCoaches: List<String>?,
    val teamTwoCoaches: List<String>?
) {
}