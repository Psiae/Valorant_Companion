package dev.flammky.valorantcompanion.live.pvp.ingame.presentation

import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.pvp.TeamID
import kotlinx.collections.immutable.persistentListOf

data class InGameTeam(
    val id: TeamID?,
    val members: List<dev.flammky.valorantcompanion.live.pvp.ingame.presentation.TeamMember>
): UNSET<dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam> by dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam.Companion {

    companion object : UNSET<dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam> {
        override val UNSET = dev.flammky.valorantcompanion.live.pvp.ingame.presentation.InGameTeam(
            null,
            emptyList()
        )
    }
}
