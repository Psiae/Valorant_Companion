package dev.flammky.valorantcompanion.pvp.mmr

import dev.flammky.valorantcompanion.pvp.RateLimitInfo

class FetchSeasonalMMRResult private constructor(
    val isSuccess: Boolean,
    val data: SeasonalMMRData?,
    val rateLimitInfo: RateLimitInfo?
) {

    companion object {

        fun success(data: SeasonalMMRData) = FetchSeasonalMMRResult(
            isSuccess = true,
            data = data,
            rateLimitInfo = null
        )

        fun failure(rateLimitInfo: RateLimitInfo) = FetchSeasonalMMRResult(
            isSuccess = false,
            data = null,
            rateLimitInfo = rateLimitInfo
        )
    }
}

inline fun FetchSeasonalMMRResult.onSuccess(
    block: (data: SeasonalMMRData) -> Unit
) {
    if (isSuccess) block(data!!)
}

inline fun FetchSeasonalMMRResult.getOrElse(
    block: (self: FetchSeasonalMMRResult) -> SeasonalMMRData
) : SeasonalMMRData {
    return if (isSuccess) data!! else block(this)
}

val FetchSeasonalMMRResult.isRateLimited: Boolean
    get() = rateLimitInfo != null

inline fun FetchSeasonalMMRResult.onRateLimited(
    block: (info: RateLimitInfo) -> Unit
) {
    if (isRateLimited) block(rateLimitInfo!!)
}

