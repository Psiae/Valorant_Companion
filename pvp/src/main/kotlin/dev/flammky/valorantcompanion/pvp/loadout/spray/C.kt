package dev.flammky.valorantcompanion.pvp.loadout.spray

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

typealias PvpSprayConstants = C

object C {
    const val SLOT_COUNT = 4

    const val SLOT_1_UUID = "5863985e-43ac-b05d-cb2d-139e72970014"

    const val SLOT_2_UUID = "7cdc908e-4f69-9140-a604-899bd879eed1"

    const val SLOT_3_UUID = "0814b2fe-4512-60a4-5288-1fbdcec6ca48"

    const val SLOT_4_UUID = "04af080a-4071-487b-61c0-5b9c0cfaac74"

    const val NO_SPRAY_UUID = "d7efbdd5-4a77-f858-a133-cfb8956ca1fe"
}

// rotation of each 90 degree angle.
fun PvpSpray.spraySlotUUIDOf4CircularIndex(index: Int): String? = when((index + 1) * (360 / 4) % 360) {
    90 -> C.SLOT_1_UUID
    180 -> C.SLOT_2_UUID
    270 -> C.SLOT_3_UUID
    0 -> C.SLOT_4_UUID
    else -> null
}

fun PvpSpray.spraySlotUUID(num: Int): String? = when(num) {
    1 -> C.SLOT_1_UUID
    2 -> C.SLOT_2_UUID
    3 -> C.SLOT_3_UUID
    4 -> C.SLOT_4_UUID
    else -> null
}

fun PvpSpray.spraySlotNum(uuid: String): Int? = when(uuid) {
    C.SLOT_1_UUID -> 1
    C.SLOT_2_UUID -> 2
    C.SLOT_3_UUID -> 3
    C.SLOT_4_UUID -> 4
    else -> null
}

fun PvpSpray.knownSlotsUUIDtoOrderedList(): ImmutableList<String> = persistentListOf(
    C.SLOT_1_UUID,
    C.SLOT_2_UUID,
    C.SLOT_3_UUID,
    C.SLOT_4_UUID
)

object PvpSpray {
}