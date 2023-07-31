package dev.flammky.valorantcompanion.live.pvp.ingame.presentation

import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.pvp.TeamID
import kotlinx.collections.immutable.persistentListOf

data class InGameTeam(
    val id: TeamID?,
    val members: List<TeamMember>
): UNSET<InGameTeam> by Companion {

    companion object : UNSET<InGameTeam> {
        override val UNSET = InGameTeam(
            null,
            emptyList()
        )
    }
}
