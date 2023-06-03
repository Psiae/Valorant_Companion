package dev.flammky.valorantcompanion.pvp.pregame

sealed interface PreGamePlayerState {

    object JOINED : PreGamePlayerState

    data class ELSE(val str: String) : PreGamePlayerState

    companion object {

        fun parse(str: String): PreGamePlayerState? {
            return when {
                str.isBlank() -> null
                str.lowercase() == "joined" -> JOINED
                else -> ELSE(str)
            }
        }
    }
}