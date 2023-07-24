package dev.flammky.valorantcompanion.pvp.pregame

import dev.flammky.valorantcompanion.pvp.date.ISO8601
import kotlinx.collections.immutable.ImmutableList

// TODO: make so that UI do less calculations
data class PreGameMatchData(
    val match_id: String,
    val version: Long,
    val teams: ImmutableList<PreGameTeam>,
    val allyTeam: PreGameTeam?,
    val enemyTeam: PreGameTeam?,
    val enemyTeamSize: Int,
    val enemyTeamLockCount: Int,
    val state: PreGameState,
    val lastUpdated: ISO8601,
    val mapID: String,
    val team1: String,
    val gamePodId: String,
    val gameModeId: String,
    val queueID: String,
    val provisioningFlow: String,
    val phaseTimeRemainingNS: Long,
    val stepTimeRemainingNS: Long,
)
