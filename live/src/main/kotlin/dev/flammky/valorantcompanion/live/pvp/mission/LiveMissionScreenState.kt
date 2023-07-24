package dev.flammky.valorantcompanion.live.pvp.mission

import dev.flammky.valorantcompanion.base.UNSET

data class LiveMissionScreenState(
    val hasError: Boolean
    // use extension instead ?
): UNSET<LiveMissionScreenState> by Companion {

    companion object : UNSET<LiveMissionScreenState> {

        override val UNSET: LiveMissionScreenState = LiveMissionScreenState(
            hasError = false
        )
    }
}