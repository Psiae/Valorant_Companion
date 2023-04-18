package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.LazyConstructor
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.constructOrThrow
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.valueOrNull
import dev.flammky.valorantcompanion.auth.riot.UserInfoHttpRequestResponse
import dev.flammky.valorantcompanion.auth.riot.UserInfoRequestResponseData
import dev.flammky.valorantcompanion.auth.riot.UserInfoRequestSession

class UserInfoRequestSessionImpl(

): UserInfoRequestSession {
    private val _firstEx = LazyConstructor<Exception?>()
    private val _response = LazyConstructor<UserInfoHttpRequestResponse>()
    private val _data = LazyConstructor<UserInfoRequestResponseData>()

    fun onResponse(
        response: UserInfoHttpRequestResponse
    ) {
        _response.constructOrThrow(response)
    }

    fun onParse(
        data: UserInfoRequestResponseData
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