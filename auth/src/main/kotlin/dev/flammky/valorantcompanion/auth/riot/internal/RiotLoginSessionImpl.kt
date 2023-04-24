package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.LazyConstructor
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.constructOrThrow
import dev.flammky.valorantcompanion.auth.LazyConstructor.Companion.valueOrNull
import dev.flammky.valorantcompanion.auth.riot.*
import kotlinx.serialization.json.JsonElement

internal class RiotLoginSessionImpl : RiotLoginSession {

    private val lock = Any()
    private var completed = false
    private val _auth = AuthRequestSessionImpl()
    private val _cookie = CookieRequestSessionImpl()
    private val _entitlement = EntitlementRequestSessionImpl()
    private val _userInfo = UserInfoRequestSessionImpl()
    private val _regionInfo = RegionInfoRequestSessionImpl()
    private val completionEx = LazyConstructor<Exception?>()
    private val invokeOnCompletion = mutableListOf<() -> Unit>()

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
    override val regionInfo: RegionInfoRequestSessionImpl
        get() = _regionInfo
    override val ex: Exception?
        get() = completionEx.valueOrNull()
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