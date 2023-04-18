package dev.flammky.valorantcompanion.auth

import androidx.annotation.GuardedBy

class UserAccountManager() {
    val registry = UserAccountRegistry()
}

class UserAccountRegistry() {

    private val lock = Any()

    @GuardedBy("lock")
    private val map = mutableMapOf<String, AuthenticatedAccount>()
        get() {
            check(Thread.holdsLock(lock))
            return field
        }

    fun registerAuthenticatedAccount(
        account: AuthenticatedAccount
    ) {
        synchronized(lock) {
            map.put(account.model.id, account)
        }
    }
}