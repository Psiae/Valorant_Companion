package dev.flammky.valorantcompanion.pvp.party

data class ErrorNotification(
    val errorType: String,
    val erroredPlayers: List<String>?
)
