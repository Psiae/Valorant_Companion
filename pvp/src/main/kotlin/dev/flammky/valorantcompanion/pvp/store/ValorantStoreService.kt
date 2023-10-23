package dev.flammky.valorantcompanion.pvp.store

interface ValorantStoreService {

    fun createClient(
        user: String
    ): ValorantUserStoreClient
}