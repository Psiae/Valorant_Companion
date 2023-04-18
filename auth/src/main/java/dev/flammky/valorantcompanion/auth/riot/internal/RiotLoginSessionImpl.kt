package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.LazyConstructor
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.constructOrThrow
import dev.flammky.valorantcompanion.auth.riot.*
import kotlinx.serialization.json.JsonElement

internal class RiotLoginSessionImpl : RiotLoginSession {

    private val lock = Any()
    private var completed = false
    private val auth = AuthRequestSessionImpl()
    private val cookie = CookieRequestSessionImpl()
    private val entitlement = EntitlementRequestSessionImpl()
    private val userInfo = UserInfoRequestSessionImpl()
    private val completionEx = LazyConstructor<Exception?>()
    private val invokeOnCompletion = mutableListOf<() -> Unit>()

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideAuthResponse(
        status: Int,
        json: JsonElement,
    ) = synchronized(lock) {
        check(!completed)
        auth.onResponse(AuthHttpRequestResponse(status, json))
    }

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideCookieResponse(
        status: Int,
        json: JsonElement
    ) = synchronized(lock) {
        check(!completed)
        cookie.onResponse(CookieHttpRequestResponse(status, json))
    }

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideEntitlementResponse(
        status: Int,
        json: JsonElement
    ) = synchronized(lock) {
        check(!completed)
        entitlement.onResponse(EntitlementHttpRequestResponse(status, json))
    }

    fun provideEntitlementData(
        data: EntitlementRequestResponseData
    ) = synchronized(lock) {
        check(!completed)
        entitlement.onParse(data)
    }

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideUserInfoResponse(
        status: Int,
        json: JsonElement
    ) = synchronized(lock) {
        check(!completed)
        userInfo.onResponse(UserInfoHttpRequestResponse(status, json))
    }

    @kotlin.jvm.Throws(IllegalStateException::class)
    fun provideUserInfoData(
        data: UserInfoRequestResponseData
    ) = synchronized(lock) {
        check(!completed)
        userInfo.onParse(data)
    }

    fun authException(ex: Exception) = synchronized(lock) {
        check(!completed)
        auth.onException(ex)
    }

    fun cookieException(ex: Exception) = synchronized(lock) {
        check(!completed)
        cookie.onException(ex)
    }

    fun entitlementException(ex: Exception) = synchronized(lock) {
        check(!completed)
        entitlement.onException(ex)
    }

    fun provideUserInfoException(ex: Exception) = synchronized(lock) {
        check(!completed)
        userInfo.onException(ex)
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

    override val authException: Exception?
        get() = auth.firstException

    override val cookieException: Exception?
        get() = cookie.firstException

    override val completionException: Exception?
        get() = completionEx.value

    override fun invokeOnCompletion(block: () -> Unit) {
        if (completed) return block()
        synchronized(lock) {
            if (completed) return block()
            invokeOnCompletion.add(block)
        }
    }
}