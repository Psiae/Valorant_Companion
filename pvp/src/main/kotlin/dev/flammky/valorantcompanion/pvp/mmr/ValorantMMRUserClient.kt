package dev.flammky.valorantcompanion.pvp.mmr

import kotlinx.coroutines.Deferred

interface ValorantMMRUserClient {

    fun fetchSeasonalMMRAsync(season: String, subject: String): Deferred<Result<SeasonalMMRData>>

    fun dispose()
}