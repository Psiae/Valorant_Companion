package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.LazyConstructor
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.constructOrThrow
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.valueOrNull
import dev.flammky.valorantcompanion.auth.riot.CookieHttpRequestResponse
import dev.flammky.valorantcompanion.auth.riot.CookieRequestSession

internal class CookieRequestSessionImpl : CookieRequestSession {
    private val _firstEx = LazyConstructor<Exception?>()
    private val _response = LazyConstructor<CookieHttpRequestResponse>()

    fun onResponse(
        response: CookieHttpRequestResponse
    ) {
        _response.constructOrThrow(response)
    }

    fun onException(
        ex: Exception
    ) {
        _firstEx.construct { ex }
    }

    override val firstException: Exception?
        get() = runCatching { _firstEx.valueOrNull() }.getOrNull()
}