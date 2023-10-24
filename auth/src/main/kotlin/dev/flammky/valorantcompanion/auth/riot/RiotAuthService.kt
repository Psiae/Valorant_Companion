package dev.flammky.valorantcompanion.auth.riot

interface RiotAuthService {

    suspend fun get_entitlement_token(puuid: String): Result<String>
    suspend fun get_authorization(puuid: String): Result<AuthRequestResponseData>
    fun createLoginClient(): RiotLoginClient
}