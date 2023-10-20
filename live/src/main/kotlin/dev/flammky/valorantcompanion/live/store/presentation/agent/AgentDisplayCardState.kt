package dev.flammky.valorantcompanion.live.store.presentation.agent

import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.base.UNSET

data class AgentDisplayCardState(
    val agentDisplayImageKey: Any,
    val agentDisplayImage: LocalImage<*>,
    val agentRoleDisplayImageKey: Any,
    val agentRoleDisplayImage: LocalImage<*>,
    val agentName: String,
): UNSET<AgentDisplayCardState> by Companion {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AgentDisplayCardState) return false

        return this.agentDisplayImageKey == other.agentDisplayImageKey &&
                this.agentRoleDisplayImageKey == other.agentRoleDisplayImageKey &&
                this.agentName == other.agentName
    }

    override fun hashCode(): Int {
        var result = 0
        result += agentDisplayImageKey.hashCode()
        result *= 31 ; result += agentRoleDisplayImageKey.hashCode()
        result *= 31 ; result += agentName.hashCode()
        return result
    }

    override fun toString(): String {
        return super.toString()
    }

    companion object : UNSET<AgentDisplayCardState> {

        override val UNSET: AgentDisplayCardState = AgentDisplayCardState(
            agentDisplayImageKey = this,
            agentDisplayImage = LocalImage.None,
            agentRoleDisplayImageKey = this,
            agentRoleDisplayImage = LocalImage.None,
            agentName = ""
        )
    }
}