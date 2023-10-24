package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.riot.RiotReauthorizeSession
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job

class RiotReauthorizeSessionImpl : RiotReauthorizeSession {

    private val completion = Job()

    val _reAuthStatusCode = atomic<Int?>(null)
    val _reAuthResponseBody = atomic<ByteArray?>(null)

    val _reAuthAccessToken = atomic<String?>(null)
    val _reAuthIdToken = atomic<String?>(null)
    val _entitlementToken = atomic<String?>(null)


    fun onException(ex: Exception) {}

    fun reAuthCookieResponse(
        httpStatusCode: Int,
        body: ByteArray
    ) {
        _reAuthStatusCode.compareAndSet(null, httpStatusCode)
        _reAuthResponseBody.compareAndSet(null, body)
    }

    fun onParse(

    ) {

    }

    fun unknownSSID() {

    }

    fun end() {

    }

    fun retrieveAuthAccessTokenRequestError(
        msg: String
    ) {

    }

    fun retrieveAuthAccessTokenBodyResponseError(
        msg: String
    ) {

    }

    fun retrieveAuthAccessTokenUnexpectedHttpStatusCode(
        httpStatusCode: Int
    ) {

    }

    fun retrieveAuthAccessTokenUnexpectedResponseBody(
        msg: String
    ) {

    }

    fun retrieveAuthAccessTokenUnexpectedException(
        msg: String
    ) {

    }

    fun retrieveAuthAccessTokenAuthError(
        msg: String
    ) {

    }

    fun retrieveAuthAccessTokenSessionError(
        msg: String
    ) {

    }

    fun reAuthCookieResponseParsed(
        access_token: String,
        id_token: String
    ) {

    }

    fun retrieveEntitlementTokenRequestError(
        msg: String
    ) {

    }

    fun retrieveEntitlementTokenBodyResponseError(
        msg: String
    ) {

    }

    fun retrieveEntitlementUnexpectedHttpStatusCode(
        httpStatusCode: Int
    ) {

    }

    fun retrieveEntitlementUnexpectedResponseBody(
        msg: String
    ) {

    }

    fun retrieveEntitlementUnexpectedException(
        msg: String
    ) {

    }

    fun retrieveEntitlementAuthDenied(
        msg: String
    ) {

    }

    fun entitlement(
        token: String
    ) {
        _entitlementToken.compareAndSet(null, token)
    }

    override fun asCoroutineJob(): Job {
        return completion
    }
}