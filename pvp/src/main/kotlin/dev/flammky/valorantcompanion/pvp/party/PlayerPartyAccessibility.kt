package dev.flammky.valorantcompanion.pvp.party

sealed class PlayerPartyAccessibility {

    object OPEN : PlayerPartyAccessibility()

    object CLOSED: PlayerPartyAccessibility()

    companion object {

        fun fromStr(str: String): PlayerPartyAccessibility? {
            return when(str.lowercase()) {
                "open" -> OPEN
                "closed" -> CLOSED
                else -> null
            }
        }

        fun fromStrStrict(str: String): PlayerPartyAccessibility? {
            return when(str) {
                "OPEN" -> OPEN
                "CLOSED" -> CLOSED
                else -> null
            }
        }
    }
}