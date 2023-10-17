package dev.flammky.valorantcompanion.assets.kotlinx.serialization.json

import dev.flammky.valorantcompanion.assets.ex.JsonParsingException
import kotlinx.serialization.json.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

internal fun unexpectedJsonElementError(
    propertyName: String,
    expectedElement: KClass<out JsonElement>,
    got: JsonElement
): Nothing = jsonParsingError(
    "expected $propertyName to be ${expectedElement.simpleName} " +
            "but got ${got::class.simpleName} instead"
)

internal fun unexpectedJsonArrayElementError(
    arrayName: String,
    expectedElement: KClass<out JsonElement>,
    got: JsonElement
): Nothing = jsonParsingError(
    "expected element of $arrayName to be ${expectedElement.simpleName} " +
            "but got ${got::class.simpleName} instead"
)

internal fun unexpectedJsonValueError(
    propertyName: String,
    message: String
): Nothing = jsonParsingError(
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

@OptIn(ExperimentalContracts::class)
internal fun JsonElement.expectJsonObject(
    propertyName: String
): JsonObject {
    contract {
        returns() implies (this@expectJsonObject is JsonObject)
    }
    return this as? JsonObject
        ?: unexpectedJsonElementError(propertyName, JsonObject::class, this)
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

internal fun JsonObject.expectJsonProperty(
    vararg propertyName: String
): JsonElement {
    propertyName.forEach { name -> get(name)?.let { return it } }
    missingJsonPropertyError(*propertyName)
}

@OptIn(ExperimentalContracts::class)
internal fun JsonElement.expectJsonObjectAsJsonArrayElement(
    arrayName: String
): JsonObject {
    contract {
        returns() implies (this@expectJsonObjectAsJsonArrayElement is JsonObject)
    }
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

/**
 * @return [null] if the receiver is [JsonNull]
 */
internal fun JsonElement.jsonNullable(): JsonElement? = if (this is JsonNull) null else this

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
                jsonParsingError(
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
                else -> jsonParsingError(
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

            if (digit < 0) jsonParsingError(
                "expected JsonNumber but got non-numeric char (${currentChar.code}) instead",
            )

            if (result < limitBeforeMul) {
                if (limitBeforeMul == limitForMaxRadix) {
                    limitBeforeMul = limit / radix

                    if (result < limitBeforeMul) jsonParsingError(
                        "expected JsonNumber to be within Int32 bounds but overflows",
                    )
                } else {
                    jsonParsingError(
                        "expected JsonNumber to be within Int32 bounds but overflows",
                    )
                }
            }

            result *= radix

            if (result < limit + digit) jsonParsingError(
                "expected JsonNumber to be within Java Int bounds but overflows",
            )

            result -= digit
        }

        if (isNegative) result else -result
    }
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
                "expected JsonNumber but value was not preceded by a leading sign nor a number"
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
            "expected JsonBoolean but value contains digit char"
        )
        content != "true" && content != "false" -> unexpectedJsonValueError(
            propertyName,
            "expected JsonBoolean but value is not a boolean"
        )
    }
}

internal fun expectJsonBooleanParseJavaBoolean(
    propertyName: String,
    content: String,
): Boolean {
    expectJsonBoolean(propertyName, content)
    return content == "true"
}

internal fun jsonParsingError(
    message: String
): Nothing {
    throw JsonParsingException(message)
}

internal fun missingJsonObjectPropertyError(
    message: String
): Nothing {
    throw JsonParsingException(message)
}