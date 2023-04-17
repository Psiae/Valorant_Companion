package dev.flammky.valorantcompanion.auth

import androidx.annotation.GuardedBy

class RiotAccountManager() {
    val registry = RiotAccountRegistry()
}

class RiotAccountRegistry() {

    private val lock = Any()

    @GuardedBy("lock")
    private val map = mutableMapOf<String, RiotAuthenticatedAccount>()
        get() {
            check(Thread.holdsLock(lock))
            return field
        }

    fun registerAuthenticatedAccount(
        account: RiotAuthenticatedAccount
    ) {
        synchronized(lock) {
            map.put(account.model.id, account)
        }
    }
}