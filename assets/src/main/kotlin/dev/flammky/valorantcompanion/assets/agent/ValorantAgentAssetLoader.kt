package dev.flammky.valorantcompanion.assets.agent

import dev.flammky.valorantcompanion.assets.internal.ValorantAssetRepository
import dev.flammky.valorantcompanion.pvp.agent.ValorantAgentIdentity
import kotlinx.collections.immutable.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

interface ValorantAgentAssetLoader {

    fun loadLiveAgentsUUIDsAsync(): Deferred<Result<ImmutableSet<String>>>
}

internal class ValorantAgentAssetLoaderImpl(
    private val coroutineScope: CoroutineScope,
    private val repository: ValorantAssetRepository
): ValorantAgentAssetLoader {

    override fun loadLiveAgentsUUIDsAsync(): Deferred<Result<ImmutableSet<String>>> {
        return CompletableDeferred(
            value = Result.success(
                ValorantAgentIdentity
                    .iterable()
                    .mapTo(
                        persistentSetOf<String>().builder(),
                        transform = { identity -> identity.uuid }
                    )
                    .build()
            )
        )
    }
}