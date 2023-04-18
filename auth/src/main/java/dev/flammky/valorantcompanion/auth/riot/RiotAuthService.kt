package dev.flammky.valorantcompanion.auth.riot

import kotlinx.coroutines.Deferred

interface RiotAuthService {

    fun loginAsync(
        request: RiotLoginRequest
    ): Deferred<RiotLoginRequestResult>
}