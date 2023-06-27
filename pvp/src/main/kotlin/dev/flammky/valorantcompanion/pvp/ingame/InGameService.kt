package dev.flammky.valorantcompanion.pvp.ingame

interface InGameService {

    fun createUserClient(puuid: String): InGameUserClient
}