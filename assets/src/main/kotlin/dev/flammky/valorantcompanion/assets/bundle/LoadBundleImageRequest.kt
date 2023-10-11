package dev.flammky.valorantcompanion.assets.bundle

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toPersistentSet

class LoadBundleImageRequest private constructor(
    val uuid: String,
    val acceptableTypes: ImmutableSet<BundleImageType>
) {

    constructor(
        uuid: String,
        vararg acceptableTypes: BundleImageType
    ): this(
        uuid = uuid,
        acceptableTypes = acceptableTypes.asIterable().toPersistentSet()
    )
}