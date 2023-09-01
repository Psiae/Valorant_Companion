package dev.flammky.valorantcompanion.pvp.internal

internal interface AuthProvider {

    suspend fun get_entitlement_token(puuid: String): Result<String>
    suspend fun get_authorization_token(puuid: String): Result<AuthorizationTokens>
}