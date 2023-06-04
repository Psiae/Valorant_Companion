package dev.flammky.valorantcompanion.pvp.pregame

sealed interface PreGameState {

    object CHARACTER_SELECT_ACTIVE : PreGameState

    object CHARACTER_SELECT_FINISHED : PreGameState

    object PROVISIONED : PreGameState

    companion object {
        fun parse(str: String): PreGameState? = when (str.lowercase()) {
            "character_select_active" -> CHARACTER_SELECT_ACTIVE
            "character_select_finished" -> CHARACTER_SELECT_FINISHED
            "provisioned" -> PROVISIONED
            else -> null
        }
    }
}