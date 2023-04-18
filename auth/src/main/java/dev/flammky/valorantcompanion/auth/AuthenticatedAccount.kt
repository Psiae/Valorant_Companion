package dev.flammky.valorantcompanion.auth

import dev.flammky.valorantcompanion.auth.riot.AccountModel

abstract class AuthenticatedAccount(
    val model: AccountModel
)