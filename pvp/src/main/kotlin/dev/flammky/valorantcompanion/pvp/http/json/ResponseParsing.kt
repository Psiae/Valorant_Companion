package dev.flammky.valorantcompanion.pvp.http.json

import dev.flammky.valorantcompanion.pvp.http.json.uselibsinternal.ScreenFloatValueRegEx
import dev.flammky.valorantcompanion.pvp.http.missingJsonObjectPropertyError
import dev.flammky.valorantcompanion.pvp.http.unexpectedResponseError
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

internal fun unexpectedJsonElementError(
    propertyName: String,
    expectedElement: KClass<out JsonElement>,
    got: JsonElement
): Nothing = unexpectedResponseError(
    "expected $propertyName to be ${expectedElement.simpleName} " +
            "but got ${got::class.simpleName} instead"
)

internal fun unexpectedJsonArrayElementError(
    arrayName: String,
    expectedElement: KClass<out JsonElement>,
    got: JsonElement
): Nothing = unexpectedResponseError(
    "expected element of $arrayName to be ${expectedElement.simpleName} " +
            "but got ${got::class.simpleName} instead"
)

internal fun unexpectedJsonValueError(
    propertyName: String,
    message: String
): Nothing = unexpectedResponseError(
    "value of $propertyName was unexpected, message: $message"
)

internal fun missingJsonPropertyError(
    propertyName: String
): Nothing = missingJsonObjectPropertyError(
    "$propertyName property not found"
)

internal fun missingJsonPropertyError(
    vararg propertyName: String
): Nothing = missingJsonObjectPropertyError(
    "none of ${propertyName.contentToString()} property were found"
)

internal fun JsonElement.expectJsonPrimitive(
    propertyName: String
): JsonPrimitive {
    return this as? JsonPrimitive
        ?: unexpectedJsonElementError(propertyName, JsonPrimitive::class, this)
}

internal fun JsonElement.expectJsonPrimitiveAsArrayElement(
    propertyName: String
): JsonPrimitive {
    return this as? JsonPrimitive
        ?: unexpectedJsonArrayElementError(propertyName, JsonPrimitive::class, this)
}

internal fun JsonElement.expectJsonObject(
    propertyName: String
): JsonObject {
    return this as? JsonObject
        ?: unexpectedJsonElementError(propertyName, JsonObject::class, this)
}

internal fun JsonElement.expectJsonObject(
    propertyName: String,
    fallback: () -> Nothing
): JsonObject {
    return this as? JsonObject
        ?: fallback()
}

internal fun JsonElement.expectJsonArray(
    propertyName: String
): JsonArray {
    return this as? JsonArray
        ?: unexpectedJsonElementError(propertyName, JsonArray::class, this)
}

internal fun JsonObject.expectJsonProperty(
    propertyName: String
): JsonElement {
    return get(propertyName)
        ?: missingJsonPropertyError(propertyName)
}

internal fun JsonObject.expectJsonPropertyBothLetterCase(
    propertyName: String
): JsonElement {
    return get(propertyName.lowercase())
        ?: get(propertyName.uppercase())
        ?: missingJsonPropertyError(propertyName)
}

internal fun JsonObject.expectJsonProperty(
    vararg propertyName: String
): JsonElement {
    propertyName.forEach { name -> get(name)?.let { return it } }
    missingJsonPropertyError(*propertyName)
}

internal fun JsonElement.expectJsonObjectAsJsonArrayElement(
    arrayName: String
): JsonObject {
    return this as? JsonObject
        ?: unexpectedJsonArrayElementError(arrayName, JsonObject::class, this)
}

internal fun JsonPrimitive.expectNotJsonNull(
    propertyName: String
): JsonPrimitive {
    if (this is JsonNull) unexpectedJsonValueError(
        propertyName,
        "value is JsonNull"
    )
    return this
}

internal inline fun JsonElement.ifJsonNull(
    block: () -> Unit
): JsonElement {
    if (this is JsonNull) block()
    return this
}

internal fun JsonElement.jsonNullable(): JsonElement? = if (this is JsonNull) null else this
internal fun JsonPrimitive.jsonNullable(): JsonPrimitive? = if (this is JsonNull) null else this

internal fun JsonPrimitive.expectNonBlankJsonString(
    propertyName: String
): JsonPrimitive {
    expectNotJsonNull(propertyName)
    expectNonBlankJsonString(propertyName, content)
    return this
}


internal fun JsonPrimitive.expectJsonNumber(
    propertyName: String
): JsonPrimitive {
    expectNotJsonNull(propertyName)
    expectJsonNumber(propertyName, content)
    return this
}

internal fun JsonPrimitive.expectJsonNumberParseInt(
    propertyName: String,
): Int {
    expectNotJsonNull(propertyName)
    return content.run {

        if (isBlank()) unexpectedJsonValueError(
            propertyName,
            "expected JsonNumber but value is blank"
        )

        val radix = 10
        val length = this.length

        val start: Int
        val isNegative: Boolean
        val limit: Int

        val firstChar = this[0]
        if (firstChar < '0') {  // Possible leading sign

            if (length == 1) {
                unexpectedResponseError(
                    "expected JsonNumber but got single non-leading-sign char (${firstChar.code}) instead",
                )
            }

            start = 1

            when (firstChar) {
                '-' -> {
                    isNegative = true
                    limit = Int.MIN_VALUE
                }
                '+' -> {
                    isNegative = false
                    limit = -Int.MAX_VALUE
                }
                else -> unexpectedResponseError(
                    "expected JsonNumber but got non-leading-sign first char (${firstChar.code}) instead",
                )
            }
        } else {
            start = 0
            isNegative = false
            limit = -Int.MAX_VALUE
        }


        val limitForMaxRadix = (-Int.MAX_VALUE) / 36

        var limitBeforeMul = limitForMaxRadix
        var result = 0
        for (i in start until length) {
            val currentChar = this[i]
            val digit = Character.digit(currentChar, radix)

            if (digit < 0) unexpectedResponseError(
                "expected JsonNumber but got non-numeric char (${currentChar.code}) instead",
            )

            if (result < limitBeforeMul) {
                if (limitBeforeMul == limitForMaxRadix) {
                    limitBeforeMul = limit / radix

                    if (result < limitBeforeMul) unexpectedResponseError(
                        "expected JsonNumber to be within Java Int bounds but overflows",
                    )
                } else {
                    unexpectedResponseError(
                        "expected JsonNumber to be within Java Int bounds but overflows",
                    )
                }
            }

            result *= radix

            if (result < limit + digit) unexpectedResponseError(
                "expected JsonNumber to be within Java Int bounds but overflows",
            )

            result -= digit
        }

        if (isNegative) result else -result
    }
}

internal fun JsonPrimitive.expectJsonNumberParseLong(
    propertyName: String,
): Long {
    expectNotJsonNull(propertyName)
    return content.run {

        if (isBlank()) unexpectedJsonValueError(
            propertyName,
            "expected JsonNumber but value is blank"
        )

        val radix = 10
        val length = this.length

        val start: Int
        val isNegative: Boolean
        val limit: Long

        val firstChar = this[0]
        if (firstChar < '0') {  // Possible leading sign

            if (length == 1) {
                unexpectedResponseError(
                    "expected JsonNumber but got single non-leading-sign char (${firstChar.code}) instead",
                )
            }

            start = 1

            when (firstChar) {
                '-' -> {
                    isNegative = true
                    limit = Long.MIN_VALUE
                }
                '+' -> {
                    isNegative = false
                    limit = Long.MAX_VALUE
                }
                else -> unexpectedResponseError(
                    "expected JsonNumber but got non-leading-sign first char (${firstChar.code}) instead",
                )
            }
        } else {
            start = 0
            isNegative = false
            limit = -Long.MAX_VALUE
        }


        val limitForMaxRadix = (-Long.MAX_VALUE) / 36

        var limitBeforeMul = limitForMaxRadix
        var result = 0L
        for (i in start until length) {
            val currentChar = this[i]
            val digit = Character.digit(currentChar, radix)

            if (digit < 0) unexpectedResponseError(
                "expected JsonNumber but got non-numeric char (${currentChar.code}) instead",
            )

            if (result < limitBeforeMul) {
                if (limitBeforeMul == limitForMaxRadix) {
                    limitBeforeMul = limit / radix

                    if (result < limitBeforeMul) unexpectedResponseError(
                        "expected JsonNumber to be within Java Long bounds but overflows",
                    )
                } else {
                    unexpectedResponseError(
                        "expected JsonNumber to be within Java Long bounds but overflows",
                    )
                }
            }

            result *= radix

            if (result < limit + digit) unexpectedResponseError(
                "expected JsonNumber to be within Java Long bounds but overflows",
            )

            result -= digit
        }

        if (isNegative) result else -result
    }
}

internal fun JsonPrimitive.expectJsonNumberParseFloat(
    propertyName: String,
): Float {
    expectNotJsonNull(propertyName)
    return content.run {
        if (isBlank()) unexpectedJsonValueError(
            propertyName,
            "expected JsonNumber but value is blank"
        )
        try {
            if (ScreenFloatValueRegEx.value.matches(this)) {
                java.lang.Float.parseFloat(this)
            } else {
                unexpectedResponseError(
                    "expected JsonPrimitive to be JsonNumber but did not match regex"
                )
            }
        } catch (nfe: NumberFormatException) {
            unexpectedResponseError(
                "expected JsonNumber to be within Java Float bounds but overflows"
            )
        }
    }
}

internal fun JsonPrimitive.expectJsonBooleanParseBoolean(
    propertyName: String
): Boolean {
    val content = content
    expectJsonBoolean(propertyName, content)
    return content == "true"
}

internal fun expectNonBlankJsonString(
    propertyName: String,
    content: String
) {
    if (content.isBlank()) unexpectedJsonValueError(
        propertyName,
        "value is blank"
    )
}

internal fun expectJsonNumber(
    propertyName: String,
    content: String
) {
    if (content.isBlank()) unexpectedJsonValueError(
        propertyName,
        "expected JsonNumber but value is blank"
    )
    val firstChar = content[0]
    val numbers = if (firstChar < '0') {
        if (firstChar == '+' || firstChar == '-')
            content.drop(1)
        else
            unexpectedJsonValueError(
                propertyName,
                "expected JsonNumber but value was leaded by non-valid leading sign"
            )
    } else {
        content
    }
    if (numbers.any { !it.isDigit() }) unexpectedJsonValueError(
        propertyName,
        "expected JsonNumber but value contains non digit char"
    )
}

internal fun expectJsonBoolean(
    propertyName: String,
    content: String
) {
    when {
        content.isBlank() -> unexpectedJsonValueError(
            propertyName,
            "expected JsonBoolean but value is blank"
        )
        content.any { it.isDigit() } -> unexpectedJsonValueError(
            propertyName,
            "expected JsonNumber but value contains digit char"
        )
        content != "true" && content != "false" -> unexpectedJsonValueError(
            propertyName,
            "expected JsonBoolean but value is not a boolean"
        )
    }
}