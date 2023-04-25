package dev.flammky.valorantcompanion.pvp.pregame

import dev.flammky.valorantcompanion.pvp.util.LazyConstructor
import dev.flammky.valorantcompanion.pvp.util.LazyConstructor.Companion.valueOrNull

class AgentSelectionPlayer(val puuid: String) {

    private val _lockedAgent = LazyConstructor<Int>()

    val lockedAgent: Int?
        get() = _lockedAgent.valueOrNull()
}