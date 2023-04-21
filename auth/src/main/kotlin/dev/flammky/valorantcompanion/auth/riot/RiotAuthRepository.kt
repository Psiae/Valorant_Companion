package dev.flammky.valorantcompanion.auth.riot

import dev.flammky.valorantcompanion.auth.AuthenticatedAccount

interface RiotAuthRepository {

    val activeAccount: AuthenticatedAccount?

    fun registerAuthenticatedAccount(
        account: RiotAuthenticatedAccount,
        setActive: Boolean
    )

    fun setActiveAccount(
        puuid: String
    )

    fun registerActiveAccountChangeListener(handler: ActiveAccountListener)

    fun unRegisterActiveAccountListener(handler: ActiveAccountListener)
}

fun interface ActiveAccountListener {

    fun onChange(old: AuthenticatedAccount?, new: AuthenticatedAccount?)
}