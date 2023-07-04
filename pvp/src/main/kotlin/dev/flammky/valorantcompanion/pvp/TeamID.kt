package dev.flammky.valorantcompanion.pvp

sealed class TeamID {

    object RED : TeamID()

    object BLUE : TeamID()

    companion object {

        fun parse(
            str: String
        ): TeamID? = when (str.lowercase()) {
            "red" -> RED
            "blue" -> BLUE
            else -> null
        }

        fun not(id: TeamID): TeamID = if (id != BLUE) BLUE else RED
    }
}
