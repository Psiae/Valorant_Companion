package dev.flammky.valorantcompanion.pvp.pregame

sealed interface PreGameCharacterSelectionState {

    object NONE : PreGameCharacterSelectionState

    object SELECTED : PreGameCharacterSelectionState

    object LOCKED : PreGameCharacterSelectionState

    companion object {

        fun parse(str: String): PreGameCharacterSelectionState? {
            return when (str.lowercase()) {
                "" -> NONE
                "selected" -> SELECTED
                "locked" -> LOCKED
                else -> null
            }
        }
    }
}