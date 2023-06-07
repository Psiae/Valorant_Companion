package dev.flammky.valorantcompanion.auth

import dev.flammky.valorantcompanion.auth.riot.AccountModel

abstract class AuthenticatedAccount() {
    abstract val model: AccountModel

    object UNSET : AuthenticatedAccount() {
        override val model: AccountModel = AccountModel.UNSET
    }
}