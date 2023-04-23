package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.LazyConstructor
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.constructOrThrow
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.valueOrNull
import dev.flammky.valorantcompanion.auth.riot.*

class RegionInfoRequestSessionImpl : RegionInfoRequestSession {
    private val _firstEx = LazyConstructor<Exception?>()
    private val _response = LazyConstructor<RegionInfoHttpRequestResponse>()
    private val _data = LazyConstructor<RegionInfoRequestResponseData>()

    fun onResponse(
        response: RegionInfoHttpRequestResponse
    ) {
        _response.constructOrThrow(response)
    }

    fun onParse(
        data: RegionInfoRequestResponseData
    ) {
        _data.constructOrThrow(data)
    }

    fun onException(
        ex: Exception
    ) {
        _firstEx.construct { ex }
    }

    override val firstException: Exception?
        get() = runCatching { _firstEx.valueOrNull() }.getOrNull()

    override val data: RegionInfoRequestResponseData?
        get() = _data.valueOrNull()
}