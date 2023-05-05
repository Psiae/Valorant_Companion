package dev.flammky.valorantcompanion.live.party.presentation

import android.os.Bundle
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration

@Immutable
data class PlayerPartyData(
    val partyID: String,
    val matchmakingQueueID: String,
    val members: ImmutableList<PlayerPartyMemberInfo>,
    val eligible: ImmutableList<String>,
    val preferredPods: ImmutableList<String>,
    val inQueue: Boolean,
    val timeStamp: Duration
) {

    companion object {

        val UNSET = PlayerPartyData(
            "",
            "",
            persistentListOf(),
            persistentListOf(),
            persistentListOf(),
            false,
            Duration.ZERO
        )
    }
}
