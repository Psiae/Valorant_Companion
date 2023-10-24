package dev.flammky.valorantcompanion.auth.riot.internal

import dev.flammky.valorantcompanion.auth.riot.AuthRequestResponseData
import dev.flammky.valorantcompanion.auth.riot.RiotAuthService
import dev.flammky.valorantcompanion.auth.riot.RiotLoginClient

internal class RiotAuthServiceImpl(
    private val authRepo: RiotAuthRepositoryImpl,
    private val geo: RiotGeoRepositoryImpl
) : RiotAuthService {

    override suspend fun get_entitlement_token(puuid: String): Result<String> {
        return runCatching { authRepo.getEntitlementToken(puuid) ?: TODO() }
    }

    override suspend fun get_authorization(puuid: String): Result<AuthRequestResponseData> {
        return runCatching {
            val access_token = authRepo.getAccessToken(puuid)
            val id_token = authRepo.getIdToken(puuid)
            if (id_token != null && access_token != null) {
                AuthRequestResponseData(access_token, id_token)
            } else TODO()
        }
    }


    override fun createLoginClient(): RiotLoginClient = KtorRiotLoginClient(authRepo, geo)
}