package dev.flammky.valorantcompanion.auth

import androidx.annotation.GuardedBy
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import io.ktor.client.plugins.auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal class UserAccountRegistry() {

    private val lock = Any()

    @GuardedBy("lock")
    private val map = mutableMapOf<String, AuthenticatedAccount>()
        get() {
            check(Thread.holdsLock(lock))
            return field
        }

    private val _entitlements = mutableMapOf<String, String>()

    private val _idTokens = mutableMapOf<String, String>()

    private val _accessTokens = mutableMapOf<String, String>()

    private val _coroutineScope = CoroutineScope(SupervisorJob())

    // should use channel instead
    private val activeAccountListeners = mutableListOf<ActiveAccountListener>()

    var activeAccount: AuthenticatedAccount? = null
        private set

    fun registerAuthenticatedAccount(
        account: AuthenticatedAccount,
        setActive: Boolean
    ) {
        synchronized(lock) {
            map[account.model.id] = account
            if (setActive) setActiveAccount(account.model.id)
        }
    }

    fun setActiveAccount(
        id: String
    ) {
        val current: AuthenticatedAccount?
        val get: AuthenticatedAccount?
        synchronized(lock) {
            get = map[id]
            if (activeAccount == get) return
            current = activeAccount
            activeAccount = get
            // TODO: we should not care about this, should use flow / channel instead
            _coroutineScope.launch(Dispatchers.Main) {
                activeAccountListeners.forEach { it.onChange(current, get) }
            }
        }
    }

    fun registerActiveAccountListener(
        handler: ActiveAccountListener
    ) {
        synchronized(lock) {
            activeAccountListeners.add(handler)
            handler.onChange(null, activeAccount)
        }

    }

    fun unRegisterActiveAccountListener(
        handler: ActiveAccountListener
    ) {
        synchronized(lock) {
            activeAccountListeners.remove(handler)
        }
    }

    fun updateEntitlementToken(
        id: String,
        token: String
    ) {
        synchronized(lock) {
            if (map[id] == null) return
            _entitlements[id] = token
        }
    }

    fun updateIdToken(
        id: String,
        token: String
    ) {
        synchronized(lock) {
            if (map[id] == null) return
            _idTokens[id] = token
        }
    }

    fun updateAccessToken(
        id: String,
        token: String
    ) {
        synchronized(lock) {
            if (map[id] == null) return
            _accessTokens[id] = token
        }
    }

    fun getEntitlementToken(id: String): String? {
        return synchronized(lock) {
            _entitlements[id]
        }
    }

    fun getIdToken(id: String): String? {
        return synchronized(lock) {
            _idTokens[id]
        }
    }

    fun getAccessToken(id: String): String? {
        return synchronized(lock) {
            _accessTokens[id]
        }
    }
}