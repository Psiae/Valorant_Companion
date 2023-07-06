package dev.flammky.valorantcompanion.pvp.mmr

import dev.flammky.valorantcompanion.pvp.PVPAsyncRequestResult
import kotlinx.coroutines.Deferred

interface ValorantMMRUserClient {

    // non-cached version
    fun fetchSeasonalMMRAsync(
        season: String,
        subject: String
    ): Deferred<PVPAsyncRequestResult<FetchSeasonalMMRResult>>

    // cached version, should only be called when subject is a part of an active match,
    // this way we can avoid rate-limit more efficiently
    fun fetchSeasonalMMRAsync(
        season: String,
        subject: String,
        activeMatch: String
    ): Deferred<PVPAsyncRequestResult<FetchSeasonalMMRResult>>

    fun dispose()
}