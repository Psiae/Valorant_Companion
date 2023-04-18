package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.LazyConstructor
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.constructOrThrow
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.valueOrNull
import dev.flammky.valorantcompanion.auth.riot.EntitlementHttpRequestResponse
import dev.flammky.valorantcompanion.auth.riot.EntitlementRequestSession

class EntitlementRequestSessionImpl : EntitlementRequestSession {
    private val _firstEx = LazyConstructor<Exception?>()
    private val _response = LazyConstructor<EntitlementHttpRequestResponse>()
    private val _data = LazyConstructor<EntitlementRequestResponseData>()

    fun onResponse(
        response: EntitlementHttpRequestResponse
    ) {
        _response.constructOrThrow(response)
    }

    fun onParse(
        data: EntitlementRequestResponseData
    ) {
        _data.constructOrThrow(data)
    }

    fun onException(
        ex: Exception
    ) {
        _firstEx.construct { ex }
    }

    override val firstException: Exception?
        get() = _firstEx.valueOrNull()
}