package dev.flammky.valorantcompanion.pvp.pregame

import dev.flammky.valorantcompanion.pvp.util.LazyConstructor

class AgentSelectionReadTeam {

    private val PLAYERS = LazyConstructor<List<AgentSelectionPlayer>>()

    fun providePlayers(players: List<AgentSelectionPlayer>) {
        PLAYERS.constructOrThrow(
            { players },
            { throw IllegalStateException("RedTeam Players was already set") }
        )
    }
}