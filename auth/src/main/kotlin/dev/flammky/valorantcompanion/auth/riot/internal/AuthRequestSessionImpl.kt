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

    fun parsedData(
        data: AuthRequestResponseData
    ) {
        _data.construct { data }
    }

    // I'm not really sure why the IllegalCastException is not caught in-place and needs to be caught here

    override val firstException: Exception?
        get() = runCatching { _firstEx.value }.getOrNull()

    val data: AuthRequestResponseData?
        get() = runCatching { _data.value }.getOrNull()
}