package dev.flammky.valorantcompanion.auth.riot.internal

import android.util.Log
import dev.flammky.valorantcompanion.auth.riot.RiotReauthorizeSession
import dev.flammky.valorantcompanion.base.kt.sync
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

class RiotJointReauthorizeSessionImpl() : RiotReauthorizeSession {

    private val completion = Job()
    private val initiated = atomic(false)
    private val killed = atomic(false)

    private val _success = atomic<Boolean?>(null)
    val initiateFlag = Job()
    val lifetime = SupervisorJob()

    override val success: Boolean
        get() = _success.value ?: false

    val _reAuthStatusCode = atomic<Int?>(null)
    val _reAuthResponseBody = atomic<ByteArray?>(null)

    val _reAuthAccessToken = atomic<String?>(null)
    val _reAuthIdToken = atomic<String?>(null)
    val _entitlementToken = atomic<String?>(null)

    private val waiterSet = mutableSetOf<Any>()

    fun addWaiter(
        waiter: Any
    ) {
        waiterSet.sync {
            if (waiterSet.add(waiter)) {
                if (waiterSet.size == 1) {
                    if (initiated.compareAndSet(expect = false, update = true)) {
                        start()
                    }
                }
            }
        }
    }

    fun removeWaiter(
        waiter: Any
    ) {
        waiterSet.sync {
            if (waiterSet.remove(waiter)) {
                if (waiterSet.size == 0) {
                    if (killed.compareAndSet(false, true)) {
                        kill()
                    }
                }
            }
        }
    }

    private fun start() {
        initiateFlag.complete()
    }

    private fun kill() {
        lifetime.cancel()
    }

    fun reAuthCookieResponse(
        httpStatusCode: Int,
        body: ByteArray
    ) {
        _reAuthStatusCode.compareAndSet(null, httpStatusCode)
        _reAuthResponseBody.compareAndSet(null, body)
    }

    fun unknownSSID() {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_unknownSSID")
    }

    fun success() {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_success")
        _success.compareAndSet(expect = null, update = true)
    }

    fun end() {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_end")
        completion.complete()
    }

    fun retrieveAuthAccessTokenRequestError(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveAuthAccessTokenRequestError($msg)")
    }

    fun retrieveAuthAccessTokenBodyResponseError(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveAuthAccessTokenBodyResponseError($msg)")
    }

    fun retrieveAuthAccessTokenUnexpectedHttpStatusCode(
        httpStatusCode: Int
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveAuthAccessTokenUnexpectedHttpStatusCode($httpStatusCode)")
    }

    fun retrieveAuthAccessTokenUnexpectedResponseBody(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveAuthAccessTokenUnexpectedResponseBody($msg)")
    }

    fun retrieveAuthAccessTokenUnexpectedException(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveAuthAccessTokenUnexpectedException($msg)")
    }

    fun retrieveAuthAccessTokenAuthError(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveAuthAccessTokenAuthError($msg)")
    }

    fun retrieveAuthAccessTokenSessionError(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveAuthAccessTokenSessionError($msg)")
    }

    fun reAuthCookieResponseParsed(
        access_token: String,
        id_token: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_reAuthCookieResponseParsed($access_token, $id_token)")
        if (!_reAuthAccessToken.compareAndSet(null, access_token)) {
            return
        }
        if (!_reAuthIdToken.compareAndSet(null, id_token)) {
            return
        }
    }

    fun retrieveEntitlementTokenRequestError(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveEntitlementTokenRequestError($msg)")
    }

    fun retrieveEntitlementTokenBodyResponseError(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveEntitlementTokenBodyResponseError($msg)")
    }

    fun retrieveEntitlementUnexpectedHttpStatusCode(
        httpStatusCode: Int
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveEntitlementUnexpectedHttpStatusCode($httpStatusCode)")
    }

    fun retrieveEntitlementUnexpectedResponseBody(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveEntitlementUnexpectedResponseBody($msg)")
    }

    fun retrieveEntitlementUnexpectedException(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveEntitlementUnexpectedException($msg)")
    }

    fun retrieveEntitlementAuthDenied(
        msg: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_retrieveEntitlementAuthDenied($msg)")
    }

    fun entitlement(
        token: String
    ) {
        Log.d("ValorantCompanion_DEBUG", "RiotReauthorizeSessionImpl_entitlement($token)")
        _entitlementToken.compareAndSet(null, token)
    }

    override fun asCoroutineJob(): Job {
        return completion
    }
}