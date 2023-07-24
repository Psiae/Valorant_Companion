package dev.flammky.valorantcompanion.base.compose.geometry

import androidx.compose.ui.geometry.Size
import dev.flammky.valorantcompanion.base.commonkt.geometry.Vector2D

operator fun Size.plus(vector2D: Vector2D): Size {
    return Size(width = width + vector2D.x, height = height + vector2D.y)
}