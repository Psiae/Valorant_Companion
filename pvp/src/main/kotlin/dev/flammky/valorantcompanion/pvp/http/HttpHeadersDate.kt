package dev.flammky.valorantcompanion.pvp.http

import java.text.SimpleDateFormat
import java.util.*

fun httpDateFormat(
    locale: Locale = Locale.US,
): SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", locale)