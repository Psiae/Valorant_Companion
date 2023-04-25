package dev.flammky.valorantcompanion.pvp.pregame

import dev.flammky.valorantcompanion.pvp.util.LazyConstructor

class AgentSelectionBlueTeam {

    private val PLAYERS = LazyConstructor<List<AgentSelectionPlayer>>()

    fun providePlayers(players: List<AgentSelectionPlayer>) {
        PLAYERS.constructOrThrow(
            { players },
            { throw IllegalStateException("BlueTeam Players was already set") }
        )
    }
}