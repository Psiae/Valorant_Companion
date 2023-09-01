package dev.flammky.valorantcompanion.pvp.store.internal

import dev.flammky.valorantcompanion.pvp.http.JsonHttpResponse
import dev.flammky.valorantcompanion.pvp.store.StoreFrontData

interface ValorantStoreResponseHandler {

    fun storeFront(response: JsonHttpResponse): Result<StoreFrontData>
}