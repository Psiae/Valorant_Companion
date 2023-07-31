package dev.flammky.valorantcompanion.assets.spray

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet

class LoadSprayImageRequest private constructor(
    internal val uuid: String,
    internal val acceptableTypes: PersistentSet<ValorantSprayImageType>
) {

    constructor(
        uuid: String,
        vararg acceptableTypes: ValorantSprayImageType
    ): this(
        uuid = uuid,
        acceptableTypes = acceptableTypes.asIterable().toPersistentSet()
    )
}