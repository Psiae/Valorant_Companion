package dev.flammky.valorantcompanion.pvp.internal

interface AuthProvider {

    suspend fun get_entitlement_token(puuid: String): Result<String>
    suspend fun get_authorization_tokens(puuid: String): Result<AuthorizationTokens>
}