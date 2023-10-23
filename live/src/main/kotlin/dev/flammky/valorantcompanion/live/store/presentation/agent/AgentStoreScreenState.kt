package dev.flammky.valorantcompanion.live.store.presentation.agent

import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import kotlinx.collections.immutable.*

data class AgentStoreScreenState(
    val agents: ImmutableSet<String>,
    val validAgents: Boolean,
    val entitledAgents: ImmutableSet<String>,
    val validEntitledAgents: Boolean,
): UNSET<AgentStoreScreenState> by Companion {

    companion object : UNSET<AgentStoreScreenState> {

        override val UNSET: AgentStoreScreenState = AgentStoreScreenState(
            agents = persistentSetOf(),
            validAgents = false,
            entitledAgents = persistentSetOf(),
            validEntitledAgents = false
        )
    }
}