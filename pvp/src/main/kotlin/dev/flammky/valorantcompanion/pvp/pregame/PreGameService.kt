package dev.flammky.valorantcompanion.pvp.pregame

interface PreGameService {

    fun createClient(puuid: String): PreGameClient
}