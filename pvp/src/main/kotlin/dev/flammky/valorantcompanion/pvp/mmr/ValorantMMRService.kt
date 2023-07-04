package dev.flammky.valorantcompanion.pvp.mmr

interface ValorantMMRService {

    fun createUserClient(puuid: String): ValorantMMRUserClient
}