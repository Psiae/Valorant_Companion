package dev.flammky.valorantcompanion.live.ingame.presentation

import dev.flammky.valorantcompanion.base.UNSET
import dev.flammky.valorantcompanion.base.C as C_BASE
import dev.flammky.valorantcompanion.assets.LocalImage
import dev.flammky.valorantcompanion.base.compose.state.SnapshotRead

data class LiveInGameTeamMemberCardState(
    val isUser: Boolean,
    val username: String?,
    val tagline: String?,
    val agentIcon: LocalImage<*>?,
    val agentIconKey: Any,
    val agentName: String?,
    val roleName: String?,
    val roleIcon: LocalImage<*>?,
    val roleIconKey: Any,
    val competitiveTierIcon: LocalImage<*>?,
    val competitiveTierIconKey: Any,
    val errorCount: Int,
    val getErrors: @SnapshotRead () -> List<LiveInGameTeamMemberCardErrorMessage>
): UNSET<LiveInGameTeamMemberCardState> {

    override val UNSET: LiveInGameTeamMemberCardState
        get() = Companion.UNSET

    companion object {
        init {
        }
        val UNSET = LiveInGameTeamMemberCardState(
            isUser = false,
            username = null,
            tagline = null,
            agentIcon = null,
            agentIconKey = C_BASE,
            agentName = null,
            roleName = null,
            roleIcon = null,
            roleIconKey = C_BASE,
            competitiveTierIcon = null,
            competitiveTierIconKey = C_BASE,
            errorCount = 0,
            getErrors = { error("") }
        )
    }
}