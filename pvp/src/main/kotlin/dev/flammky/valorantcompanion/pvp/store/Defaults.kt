package dev.flammky.valorantcompanion.pvp.store

import dev.flammky.valorantcompanion.pvp.agent.ValorantAgent
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import kotlinx.collections.immutable.persistentListOf

internal val DEFAULT_UNLOCKED_AGENTS: List<ValorantAgent> by lazy {
    persistentListOf<ValorantAgent>().builder()
        .apply {
            add(ValorantAgent.BRIMSTONE)
            add(ValorantAgent.JETT)
            add(ValorantAgent.PHOENIX)
            add(ValorantAgent.SAGE)
            add(ValorantAgent.SOVA)
        }
        .build()
}

internal val DEFAULT_UNLOCKED_AGENTS_IDENTITY: List<ValorantAgentIdentity> by lazy {
    DEFAULT_UNLOCKED_AGENTS
        .mapTo(
            destination = persistentListOf<ValorantAgentIdentity>().builder(),
            transform = ValorantAgentIdentity::of
        ).build()
}