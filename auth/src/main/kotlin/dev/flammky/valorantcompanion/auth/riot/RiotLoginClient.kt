package dev.flammky.valorantcompanion.auth.riot

interface RiotLoginClient {

    fun dispose()

    fun login(
        request: RiotLoginRequest,
        setActive: Boolean
    ): RiotLoginSession

    fun reauthorize(
        puuid: String
    ): RiotReauthorizeSession
}