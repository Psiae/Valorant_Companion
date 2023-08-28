package dev.flammky.valorantcompanion.base

fun Triple<Int, Int, Int>.nearestBlackOrWhite(): Triple<Int, Int, Int> {
    val r = first
    val g = second
    val b = third
    check(r in 0..255 && g in 0..255 && b in 0..255) {
        "given triple is not an RGB values"
    }
    val v = ((0.2126 * r) + (0.7152 * g) + (0.0722 * b)).toFloat()
    val luminance =
        if (v <= 0.0f) 0.0f
        else if (v >= 1.0f) 1.0f else v
    return if (luminance > 0.5)
        Triple(255, 255, 255)
    else
        Triple(0, 0, 0)
}