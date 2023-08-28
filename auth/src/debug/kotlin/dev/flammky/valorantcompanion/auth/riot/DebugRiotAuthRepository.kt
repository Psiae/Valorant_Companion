package dev.flammky.valorantcompanion.auth.riot

import dev.flammky.valorantcompanion.auth.AuthenticatedAccount
import dev.flammky.valorantcompanion.auth.riot.internal.RiotAuthRepositoryImpl

class DebugRiotAuthRepository : RiotAuthRepository {

    private val impl = RiotAuthRepositoryImpl()

    override val activeAccount: AuthenticatedAccount?
        get() = impl.activeAccount

    override fun registerAuthenticatedAccount(
        account: RiotAuthenticatedAccount,
        setActive: Boolean
    ) = impl.registerAuthenticatedAccount(account, setActive)

    override fun setActiveAccount(puuid: String) {
        impl.setActiveAccount(puuid)
    }

    override fun registerActiveAccountChangeListener(handler: ActiveAccountListener) {
        impl.registerActiveAccountChangeListener(handler)
    }

    override fun unRegisterActiveAccountListener(handler: ActiveAccountListener) {
        impl.unRegisterActiveAccountListener(handler)
    }
}