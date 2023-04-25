package dev.flammky.valorantcompanion.pvp.party

data class CustomGameDataSettings(
    val mapId: String,
    val mode: String,
    val useBots: Boolean,
    val gamePod: String
) {
}