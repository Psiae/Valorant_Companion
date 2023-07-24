package dev.flammky.valorantcompanion.live

fun pingStrengthInRangeOf4(pingMs: Int): Int {
    require(pingMs >= 0) {
        "Ping strength cannot be negative"
    }
    return when(pingMs) {
        in 0..45 -> 4
        in 46..70 -> 3
        in 71 .. 100 -> 2
        else -> 1
    }
}

fun pingStrengthInRangeOf3(pingMs: Int): Int {
    require(pingMs >= 0) {
        "Ping strength cannot be negative"
    }
    return when(pingMs) {
        in 0..45 -> 3
        in 46..100 -> 2
        else -> 1
    }
}