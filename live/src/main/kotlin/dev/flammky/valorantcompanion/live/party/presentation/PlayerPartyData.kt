package dev.flammky.valorantcompanion.live.party.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class PlayerPartyData(
    val partyID: String,
    val matchmakingQueueID: String,
    val members: ImmutableList<PlayerPartyMemberInfo>,
    val eligible: ImmutableList<String>,
    val preferredPods: ImmutableList<String>
) {

    companion object {
        val UNSET = PlayerPartyData(
            "",
            "",
            persistentListOf(),
            persistentListOf(),
            persistentListOf(),
        )
    }
}
