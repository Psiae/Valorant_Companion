package dev.flammky.valorantcompanion.auth

import androidx.annotation.GuardedBy
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener
import io.ktor.client.plugins.auth.*

internal class UserAccountRegistry() {

    private val lock = Any()

    @GuardedBy("lock")
    private val map = mutableMapOf<String, AuthenticatedAccount>()
        get() {
            check(Thread.holdsLock(lock))
            return field
        }

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
            activeAccountListeners.forEach { it.onChange(current, get) }
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
}