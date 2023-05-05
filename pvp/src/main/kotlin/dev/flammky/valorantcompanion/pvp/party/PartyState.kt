package dev.flammky.valorantcompanion.pvp.party

sealed class PartyState {

    object DEFAULT : PartyState()

    object STARTING_MATCHMAKING : PartyState()

    object MATCHMAKING : PartyState()

    object LEAVING_MATCHMAKING : PartyState()

    companion object {
        fun toPartyDataString(state: PartyState) = when(state) {
            is DEFAULT -> "DEFAULT"
            MATCHMAKING -> "MATCHMAKING"
            LEAVING_MATCHMAKING -> "LEAVING_MATCHMAKING"
            STARTING_MATCHMAKING -> "STARTING_MATCHMAKING"
        }

        fun fromPartyDataString(string: String): PartyState? = when(string) {
            toPartyDataString(DEFAULT) -> DEFAULT
            toPartyDataString(MATCHMAKING) -> MATCHMAKING
            toPartyDataString(LEAVING_MATCHMAKING) -> LEAVING_MATCHMAKING
            toPartyDataString(STARTING_MATCHMAKING) -> STARTING_MATCHMAKING
            else -> null
        }
    }
}
