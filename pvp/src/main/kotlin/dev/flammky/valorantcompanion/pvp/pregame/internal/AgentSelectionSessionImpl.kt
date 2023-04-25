package dev.flammky.valorantcompanion.pvp.pregame.internal

import dev.flammky.valorantcompanion.auth.riot.region.RiotRegion
import dev.flammky.valorantcompanion.auth.riot.region.RiotShard
import dev.flammky.valorantcompanion.pvp.pregame.AgentSelectionSession
import dev.flammky.valorantcompanion.pvp.pregame.AgentSelectionBlueTeam
import dev.flammky.valorantcompanion.pvp.pregame.AgentSelectionReadTeam
import dev.flammky.valorantcompanion.pvp.pregame.AgentSelectionTime

internal class AgentSelectionSessionImpl(
    val region: RiotRegion,
    val shard: RiotShard
) : AgentSelectionSession() {
    val time = AgentSelectionTime()
    val red = AgentSelectionReadTeam()
    val blue = AgentSelectionBlueTeam()
}