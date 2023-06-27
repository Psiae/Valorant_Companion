package dev.flammky.valorantcompanion.pvp.mmr

import kotlinx.coroutines.Deferred

interface MMRUserClient {

    fun fetchSeasonalMMR(season: String, subject: String): Deferred<Result<SeasonalMMRData>>
}