package dev.flammky.valorantcompanion.auth.riot

import dev.flammky.valorantcompanion.auth.AuthenticatedAccount

data class RiotAuthenticatedAccount(
    override val model: RiotAccountModel,
): AuthenticatedAccount()


