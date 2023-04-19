package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.LazyConstructor
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.constructOrThrow
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.valueOrNull
import dev.flammky.valorantcompanion.auth.riot.AuthHttpRequestResponse
import dev.flammky.valorantcompanion.auth.riot.AuthRequestResponseData
import dev.flammky.valorantcompanion.auth.riot.AuthRequestSession

class AuthRequestSessionImpl : AuthRequestSession {

    private val _firstEx = LazyConstructor<Exception?>()
    private val _response = LazyConstructor<AuthHttpRequestResponse>()
    private val _data = LazyConstructor<AuthRequestResponseData>()

    fun onResponse(
        response: AuthHttpRequestResponse
    ) {
        _response.constructOrThrow(response)
    }

    fun onException(
        ex: Exception
    ) {
        _firstEx.construct { ex }
    }

    fun onParse(
        data: AuthRequestResponseData
    ) {
        _data.construct { data }
    }

    override val firstException: Exception?
        get() = runCatching { _firstEx.valueOrNull() }.getOrNull()
}