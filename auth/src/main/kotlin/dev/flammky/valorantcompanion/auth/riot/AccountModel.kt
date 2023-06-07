package dev.flammky.valorantcompanion.auth.riot

abstract class AccountModel() {
    abstract val id: String
    abstract val username: String

    object UNSET : AccountModel() {
        override val id: String = ""
        override val username: String = ""
    }
}