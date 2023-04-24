package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.riot.*

internal class RiotAuthServiceImpl(
    private val auth: RiotAuthRepositoryImpl,
    private val geo: RiotGeoRepositoryImpl
) : RiotAuthService {

    override suspend fun get_entitlement_token(puuid: String): Result<String> {
        return runCatching { auth.getEntitlementToken(puuid) ?: TODO() }
    }

    override suspend fun get_authorization(puuid: String): Result<AuthRequestResponseData> {
        return runCatching {
            val access_token = auth.getAccessToken(puuid)
            val id_token = auth.getIdToken(puuid)
            if (id_token != null && access_token != null) {
                AuthRequestResponseData(access_token, id_token)
            } else TODO()
        }
    }

    override fun createLoginClient(): RiotLoginClient = KtorRiotLoginClient(auth, geo)
}