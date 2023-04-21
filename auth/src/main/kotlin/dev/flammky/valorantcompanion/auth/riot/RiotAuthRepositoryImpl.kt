package dev.flammky.valorantcompanion.auth.riot

import dev.flammky.valorantcompanion.auth.*

internal class RiotAuthRepositoryImpl() : RiotAuthRepository {

    private val registry = UserAccountRegistry()

    override val activeAccount: AuthenticatedAccount?
        get() = registry.activeAccount

    override fun registerAuthenticatedAccount(
        account: RiotAuthenticatedAccount,
        setActive: Boolean
    ) {
        registry.registerAuthenticatedAccount(account, setActive)
    }

    override fun setActiveAccount(puuid: String) {
        registry.setActiveAccount(puuid)
    }

    override fun registerActiveAccountChangeListener(handler: ActiveAccountListener) {
        registry.registerActiveAccountListener(handler)
    }

    override fun unRegisterActiveAccountListener(handler: ActiveAccountListener) {
        registry.unRegisterActiveAccountListener(handler)
    }
}