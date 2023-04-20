package dev.flammky.valorantcompanion.auth.riot

interface RiotAuthService {

    fun createLoginClient(): RiotLoginClient
}