package dev.flammky.valorantcompanion.auth

interface RiotAuthService {

    fun login(
        username: String,
        password: String,
        retain: String
    )

    fun loginWithGoogle() = NotImplementedError("Not Yet Implemented")
}