package dev.flammky.valorantcompanion.live.pvp.pregame.presentation

import androidx.compose.runtime.Immutable
import dev.flammky.valorantcompanion.base.UNSET
import kotlin.time.Duration

// data class for copy function convenience, use identity equality
@Immutable
data class LivePreGameScreenState(
    val user: String,
    val noOp: Boolean,
    // non-null if [noOp] is true
    val noOpMessage: String?,
    val needUserRefresh: Boolean,
    // non-null if [needUserRefresh] is true, may be empty
    val needUserRefreshMessage: String?,
    // non-null if [needUserRefresh] is true
    val needUserRefreshRunnable: (() -> Unit)?,
    val explicitLoading: Boolean,
    // non-null if [explicitLoading] is true, may be empty
    val explicitLoadingMessage: String?,
    val inMatch: Boolean?,
    // non-null if [inMatch] is true
    val matchId: String?,
    val mapName: String?,
    val gameTypeName: String?,
    val gamePodName: String?,
    val gamePodPingMs: Int?,
    val countdown: Duration?,
    // key does NOT represent structural equality of [ally]
    val allyKey: Any,
    val ally: PreGameTeam?,
    val selectAgent: (String) -> Unit,
    val lockAgent: (String) -> Unit,
): UNSET<LivePreGameScreenState> by Companion {

    override fun equals(other: Any?): Boolean = super.equals(other)

    override fun hashCode(): Int = super.hashCode()

    companion object : UNSET<LivePreGameScreenState> {

        override val UNSET: LivePreGameScreenState = LivePreGameScreenState(
            user = "",
            noOp = false,
            noOpMessage = null,
            needUserRefresh = false,
            needUserRefreshMessage = null,
            needUserRefreshRunnable = null,
            explicitLoading = false,
            explicitLoadingMessage = null,
            inMatch = false,
            matchId = null,
            mapName = null,
            gameTypeName = null,
            gamePodName = null,
            gamePodPingMs = null,
            countdown = null,
            allyKey = this,
            ally = null,
            lockAgent = {},
            selectAgent = {}
        )
    }
}