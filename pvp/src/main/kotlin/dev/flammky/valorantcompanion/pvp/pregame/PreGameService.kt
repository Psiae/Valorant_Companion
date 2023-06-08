package dev.flammky.valorantcompanion.pvp.pregame

interface PreGameService {

    fun createUserClient(puuid: String): PreGameUserClient
}