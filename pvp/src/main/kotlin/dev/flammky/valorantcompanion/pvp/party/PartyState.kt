package dev.flammky.valorantcompanion.pvp.party

sealed class PartyState {

    object DEFAULT : PartyState()

    object STARTING_MATCHMAKING : PartyState()

    object MATCHMAKING : PartyState()

    object LEAVING_MATCHMAKING : PartyState()

    object MATCHMADE_GAME_STARTING : PartyState()

    companion object {
        fun toPartyDataString(state: PartyState) = when(state) {
            DEFAULT -> "DEFAULT"
            MATCHMAKING -> "MATCHMAKING"
            LEAVING_MATCHMAKING -> "LEAVING_MATCHMAKING"
            STARTING_MATCHMAKING -> "STARTING_MATCHMAKING"
            MATCHMADE_GAME_STARTING -> "MATCHMADE_GAME_STARTING"
        }

        fun fromPartyDataString(string: String): PartyState? = when(string) {
            toPartyDataString(DEFAULT) -> DEFAULT
            toPartyDataString(MATCHMAKING) -> MATCHMAKING
            toPartyDataString(LEAVING_MATCHMAKING) -> LEAVING_MATCHMAKING
            toPartyDataString(STARTING_MATCHMAKING) -> STARTING_MATCHMAKING
            toPartyDataString(MATCHMADE_GAME_STARTING) -> MATCHMADE_GAME_STARTING
            else -> null
        }
    }
}
