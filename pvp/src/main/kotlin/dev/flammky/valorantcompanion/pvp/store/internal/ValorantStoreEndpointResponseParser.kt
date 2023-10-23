package dev.flammky.valorantcompanion.pvp.store.internal

import dev.flammky.valorantcompanion.pvp.store.ValorantStoreEntitledItems
import java.nio.charset.Charset

interface ValorantStoreEndpointResponseParser {

    fun parseGetEntitledItemResponse(
        raw: ByteArray,
        charset: Charset?
    ): Result<ValorantStoreEntitledItems>
}