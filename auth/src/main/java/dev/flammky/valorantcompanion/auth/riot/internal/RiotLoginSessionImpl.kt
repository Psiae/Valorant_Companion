package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.LazyConstructor
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.constructOrThrow
import dev.flammky.valorantcompanion.auth.riot.*
import kotlinx.serialization.json.JsonElement

internal class RiotLoginSessionImpl : RiotLoginSession {

    private val lock = Any()
    private var completed = false
    private val _auth = AuthRequestSessionImpl()
    private val _cookie = CookieRequestSessionImpl()
    private val _entitlement = EntitlementRequestSessionImpl()
    private val _userInfo = UserInfoRequestSessionImpl()
    private val completionEx = LazyConstructor<Exception?>()
    private val invokeOnCompletion = mutableListOf<() -> Unit>()

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideAuthResponse(
        status: Int,
        json: JsonElement,
    ) = synchronized(lock) {
        check(!completed)
        _auth.onResponse(AuthHttpRequestResponse(status, json))
    }

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideAuthResponseData(
        data: AuthRequestResponseData
    ) = synchronized(lock) {
        check(!completed)
        _auth.parsedData(data)
    }

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideCookieResponse(
        status: Int,
        json: JsonElement
    ) = synchronized(lock) {
        check(!completed)
        _cookie.onResponse(CookieHttpRequestResponse(status, json))
    }

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideEntitlementResponse(
        status: Int,
        json: JsonElement
    ) = synchronized(lock) {
        check(!completed)
        _entitlement.onResponse(EntitlementHttpRequestResponse(status, json))
    }

    fun provideEntitlementData(
        data: EntitlementRequestResponseData
    ) = synchronized(lock) {
        check(!completed)
        _entitlement.onParse(data)
    }

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideUserInfoResponse(
        status: Int,
        json: JsonElement
    ) = synchronized(lock) {
        check(!completed)
        _userInfo.onResponse(UserInfoHttpRequestResponse(status, json))
    }

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideUserInfoData(
        data: UserInfoRequestResponseData
    ) = synchronized(lock) {
        check(!completed)
        _userInfo.onParse(data)
    }

    fun authException(ex: Exception) = synchronized(lock) {
        check(!completed)
        _auth.onException(ex)
    }

    fun cookieException(ex: Exception) = synchronized(lock) {
        check(!completed)
        _cookie.onException(ex)
    }

    fun entitlementException(ex: Exception) = synchronized(lock) {
        check(!completed)
        _entitlement.onException(ex)
    }

    fun provideUserInfoException(ex: Exception) = synchronized(lock) {
        check(!completed)
        _userInfo.onException(ex)
    }

    fun makeCompleting(
        ex: Exception?
    ) = synchronized(lock) {
        check(!completed)
        completed = true
        completionEx.constructOrThrow(ex)
        invokeOnCompletion.forEach { it.invoke() }
        invokeOnCompletion.clear()
    }

    override val cookie: CookieRequestSessionImpl
        get() = _cookie
    override val auth: AuthRequestSessionImpl
        get() = _auth
    override val entitlement: EntitlementRequestSessionImpl
        get() = _entitlement
    override val userInfo: UserInfoRequestSessionImpl
        get() = _userInfo
    override val ex: Exception?
        get() = cookie.firstException
            ?: auth.firstException
            ?: entitlement.firstException
            ?: userInfo.firstException

    override fun invokeOnCompletion(block: () -> Unit) {
        if (!completed) {
            synchronized(lock) {
                if (!completed) {
                    invokeOnCompletion.add(block)
                    return
                }
            }
        }
        block()
    }
}