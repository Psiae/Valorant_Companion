package dev.flammky.valorantcompanion.pvp.internal

import dev.flammky.valorantcompanion.auth.riot.RiotAuthService

class AuthProviderImpl(
    private val authService: RiotAuthService
) : AuthProvider {

    override suspend fun get_entitlement_token(puuid: String): Result<String> {
        return authService.get_entitlement_token(puuid)
    }

    override suspend fun get_authorization_tokens(puuid: String): Result<AuthorizationTokens> {
       return runCatching {
           val data = authService.get_authorization(puuid).getOrThrow()
           AuthorizationTokens(data.access_token, data.id_token)
       }
    }
}