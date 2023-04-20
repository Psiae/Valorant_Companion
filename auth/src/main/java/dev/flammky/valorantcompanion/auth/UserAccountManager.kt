package dev.flammky.valorantcompanion.auth

import androidx.annotation.GuardedBy
import dev.flammky.valorantcompanion.auth.riot.ActiveAccountListener

internal class UserAccountRegistry() {

    private val lock = Any()

    @GuardedBy("lock")
    private val map = mutableMapOf<String, AuthenticatedAccount>()
        get() {
            check(Thread.holdsLock(lock))
            return field
        }

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
        synchronized(lock) {
            val get = map[id] ?: return
            if (activeAccount == get) return
            val current = activeAccount
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