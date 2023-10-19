package dev.flammky.valorantcompanion.pvp.store.internal

import dev.flammky.valorantcompanion.base.time.ISO8601
import dev.flammky.valorantcompanion.pvp.http.JsonHttpResponse
import dev.flammky.valorantcompanion.pvp.http.json.*
import dev.flammky.valorantcompanion.pvp.http.json.expectJsonObject
import dev.flammky.valorantcompanion.pvp.http.json.expectJsonPrimitive
import dev.flammky.valorantcompanion.pvp.http.json.expectJsonProperty
import dev.flammky.valorantcompanion.pvp.http.json.expectNonBlankJsonString
import dev.flammky.valorantcompanion.pvp.http.unexpectedResponseError
import dev.flammky.valorantcompanion.pvp.store.*
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCost
import dev.flammky.valorantcompanion.pvp.store.currency.StoreCurrency
import dev.flammky.valorantcompanion.pvp.store.currency.ofID
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.JsonObject
import kotlin.math.floor
import kotlin.time.Duration.Companion.seconds

class RiotValorantStoreResponseHandler : ValorantStoreResponseHandler {

    override fun storeFront(response: JsonHttpResponse): Result<StoreFrontData> {
        return runCatching {
            when (response.statusCode) {
                in 200..299 -> {
                    return@runCatching parseStoreFrontResponseBody(response).getOrElse {
                        unexpectedResponseError("UNABLE TO PARSE STOREFRONT RESPONSE BODY", it)
                    }
                }
                // 429, retry once
            }
            unexpectedResponseError(
                "UNABLE TO PARSE STOREFRONT RESPONSE BODY," +
                        " UNHANDLED HTTP STATUS CODE (${response.statusCode})"
            )
        }
    }

    override fun featuredBundleData(response: JsonHttpResponse): Result<FeaturedBundleDisplayData> {
        return runCatching {
            when (response.statusCode) {
                in 200..299 -> {
                    return@runCatching parseFeaturedBundleDataResponseBody(response).getOrElse {
                        unexpectedResponseError("UNABLE TO PARSE FEATURED BUNDLE DATA RESPONSE BODY", it)
                    }
                }
            }
            unexpectedResponseError(
                "UNABLE TO PARSE FEATURED BUNDLE DATA RESPONSE BODY," +
                        " UNHANDLED HTTP STATUS CODE (${response.statusCode})"
            )
        }
    }

    private fun parseStoreFrontResponseBody(
        response: JsonHttpResponse
    ): Result<StoreFrontData> {
        return runCatching {
            val body = response.body.getOrThrow()
            val obj = body.expectJsonObject("StoreFrontResponseBody")

            StoreFrontData(
                featuredBundleStore = parseFeaturedBundle(obj),
                skinsPanel = parseSkinsPanelData(obj),
                upgradeCurrencyStore = parseUpgradeCurrencyStore(obj),
                bonusStore = parseBonusStore(obj),
                accessoryStore = parseAccessoryStore(obj)
            )
        }
    }

    private fun parseFeaturedBundleDataResponseBody(
        response: JsonHttpResponse
    ): Result<FeaturedBundleDisplayData> {
        return runCatching {
            val body = response.body.getOrThrow()
            val obj = body
                .expectJsonObject("FeaturedBundleDataResponseBody")
                .expectJsonProperty("data")
                .expectJsonObject("data")

            FeaturedBundleDisplayData(
                uuid = obj
                    .expectJsonProperty("uuid")
                    .expectJsonPrimitive("uuid")
                    .expectNonBlankJsonString("uuid")
                    .content,
                displayName = obj
                    .expectJsonProperty("displayName")
                    .expectJsonPrimitive("displayName")
                    .expectNonBlankJsonString("displayName")
                    .content,
                displayNameSubText = obj
                    .expectJsonProperty("displayNameSubText")
                    .expectJsonPrimitive("displayNameSubText")
                    .jsonNullable()
                    ?.expectNonBlankJsonString("displayNameSubText")
                    ?.content,
                description = obj
                    .expectJsonProperty("description")
                    .expectJsonPrimitive("description")
                    .expectNonBlankJsonString("description")
                    .content,
                extraDescription = obj
                    .expectJsonProperty("extraDescription")
                    .expectJsonPrimitive("extraDescription")
                    .jsonNullable()
                    ?.expectNonBlankJsonString("extraDescription")
                    ?.content,
                useAdditionalContext = obj
                    .expectJsonProperty("useAdditionalContext")
                    .expectJsonPrimitive("useAdditionalContext")
                    .expectJsonBooleanParseBoolean("useAdditionalContext")
            )
        }
    }

    private fun parseFeaturedBundle(
        obj: JsonObject
    ): FeaturedBundleStore {

        val offer = obj["FeaturedBundle"]

        return FeaturedBundleStore(
            open = offer != null,
            offer = runCatching {
                offer?.let { offer ->
                    offer.expectJsonObject("FeaturedBundle")
                    FeaturedBundleStore.Offer(
                        bundle = run {
                            val bundleObj = offer
                                .expectJsonProperty("Bundle")
                                .expectJsonObject("Bundle")
                            parseFeaturedBundleBundle(bundleObj)
                        },
                        bundles = run {
                            val bundlesArray = offer
                                .expectJsonProperty("Bundles")
                                .expectJsonArray("Bundles")
                            bundlesArray
                                .mapTo(persistentListOf<FeaturedBundleStore.Bundle>().builder()) { element ->
                                    val bundleObj = element
                                        .expectJsonObjectAsJsonArrayElement("Bundles")
                                    parseFeaturedBundleBundle(bundleObj)
                                }
                                .build()
                        },
                        bundleRemainingDuration = run {
                            offer
                                .expectJsonProperty("BundleRemainingDurationInSeconds")
                                .expectJsonPrimitive("BundleRemainingDurationInSeconds")
                                .expectJsonNumberParseInt("BundleRemainingDurationInSeconds")
                                .seconds
                        }
                    )
                }
            }
        )
    }

    private fun parseFeaturedBundleBundle(
        bundleObj: JsonObject
    ): FeaturedBundleStore.Bundle {
        return FeaturedBundleStore.Bundle(
            id = bundleObj
                .expectJsonProperty("ID")
                .expectJsonPrimitive("ID")
                .expectNonBlankJsonString("ID")
                .content,
            dataAssetID = bundleObj
                .expectJsonProperty("DataAssetID")
                .expectJsonPrimitive("DataAssetID")
                .expectNonBlankJsonString("DataAssetID")
                .content,
            currencyID = bundleObj
                .expectJsonProperty("CurrencyID")
                .expectJsonPrimitive("CurrencyID")
                .expectNonBlankJsonString("CurrencyID")
                .content,
            itemOffers = bundleObj
                .expectJsonProperty("Items")
                .expectJsonArray("Items")
                .mapTo(persistentListOf<FeaturedBundleStore.ItemBaseOffer>().builder()) { item ->
                    val itemObj = item
                        .expectJsonObjectAsJsonArrayElement("Items")
                    val itemOfferObj = itemObj
                        .expectJsonProperty("Item")
                        .expectJsonObject("Item")
                    FeaturedBundleStore.ItemBaseOffer(
                        baseCost = run {
                            StoreCost(
                                currency = StoreCurrency.ofID(
                                    itemObj
                                        .expectJsonProperty("CurrencyID")
                                        .expectJsonPrimitive("CurrencyID")
                                        .expectNonBlankJsonString("CurrencyID")
                                        .content
                                ),
                                amount =
                                itemObj
                                    .expectJsonProperty("BasePrice")
                                    .expectJsonPrimitive("BasePrice")
                                    .expectJsonNumberParseLong("BasePrice")
                            )
                        },
                        discountPercent = run {
                            itemObj
                                .expectJsonProperty("DiscountPercent")
                                .expectJsonPrimitive("DiscountPercent")
                                .expectJsonNumberParseFloat("DiscountPercent")
                        },
                        discountedPrice = run {
                            itemObj
                                .expectJsonProperty("DiscountedPrice")
                                .expectJsonPrimitive("DiscountedPrice")
                                .expectJsonNumberParseLong("DiscountedPrice")
                        },
                        isPromoItem = run {
                            itemObj
                                .expectJsonProperty("IsPromoItem")
                                .expectJsonPrimitive("IsPromoItem")
                                .expectJsonBooleanParseBoolean("IsPromoItem")
                        },
                        reward = run {
                            FeaturedBundleStore.Reward(
                                itemType = run {
                                    val itemTypeID = itemOfferObj
                                        .expectJsonProperty("ItemTypeID")
                                        .expectJsonPrimitive("ItemTypeID")
                                        .expectNonBlankJsonString("ItemTypeID")
                                        .content
                                    ItemType.OBJECTS.find { it.id == itemTypeID }
                                        ?: ItemType.Other("", itemTypeID)
                                },
                                itemID = run {
                                    itemOfferObj
                                        .expectJsonProperty("ItemID")
                                        .expectJsonPrimitive("ItemID")
                                        .expectNonBlankJsonString("ItemID")
                                        .content
                                },
                                quantity = run {
                                    itemOfferObj
                                        .expectJsonProperty("Amount")
                                        .expectJsonPrimitive("Amount")
                                        .expectJsonNumberParseLong("Amount")
                                }
                            )
                        }
                    )

                }
                .build(),
            itemDiscountedOffers = bundleObj["ItemOffers"]
                ?.jsonNullable()
                ?.expectJsonArray("ItemOffers")
                ?.mapTo(persistentListOf<FeaturedBundleStore.ItemDiscountedOffer>().builder()) { element ->
                    val itemObj = element
                        .expectJsonObjectAsJsonArrayElement("ItemOffers")
                    val offerObj = itemObj
                        .expectJsonProperty("Offer")
                        .expectJsonObject("Offer")
                    FeaturedBundleStore.ItemDiscountedOffer(
                        offerID = run {
                            itemObj
                                .expectJsonProperty("BundleItemOfferID")
                                .expectJsonPrimitive("BundleItemOfferID")
                                .expectNonBlankJsonString("BundleItemOfferID")
                                .content
                        },
                        isDirectPurchase = run {
                            offerObj
                                .expectJsonProperty("IsDirectPurchase")
                                .expectJsonPrimitive("IsDirectPurchase")
                                .expectJsonBooleanParseBoolean("IsDirectPurchase")
                        },
                        startDate = run {
                            val dateStr = offerObj
                                .expectJsonProperty("StartDate")
                                .expectJsonPrimitive("StartDate")
                                .expectNonBlankJsonString("StartDate")
                                .content
                            ISO8601.fromISOString(dateStr)
                        },
                        baseCost = run {
                            val costObj = offerObj
                                .expectJsonProperty("Cost")
                                .expectJsonObject("Cost")
                            val currencyProperty = costObj
                                .entries
                                .also {
                                    when {
                                        it.isEmpty() -> unexpectedResponseError(
                                            "FeaturedBundle ItemOfferCost have no currency present"
                                        )
                                        it.size > 1 -> unexpectedResponseError(
                                            "FeaturedBundle ItemOfferCost have more than 1 currency present"
                                        )
                                    }
                                }
                                .first()
                            StoreCost(
                                currency = StoreCurrency.ofID(currencyProperty.key),
                                amount = currencyProperty.value
                                    .expectJsonPrimitive(currencyProperty.key)
                                    .expectJsonNumberParseLong(currencyProperty.key)
                            )
                        },
                        discountPercent = run {
                            itemObj
                                .expectJsonProperty("DiscountPercent")
                                .expectJsonPrimitive("DiscountPercent")
                                .expectJsonNumberParseFloat("DiscountPercent")
                        },
                        discountedCost = run {
                            val costObj = offerObj
                                .expectJsonProperty("Cost")
                                .expectJsonObject("Cost")
                            val currencyProperty = costObj
                                .entries
                                .also {
                                    when {
                                        it.isEmpty() -> unexpectedResponseError(
                                            "FeaturedBundle ItemOfferCost have no currency present"
                                        )
                                        it.size > 1 -> unexpectedResponseError(
                                            "FeaturedBundle ItemOfferCost have more than 1 currency present"
                                        )
                                    }
                                }
                                .first()
                            StoreCost(
                                currency = StoreCurrency.ofID(currencyProperty.key),
                                amount = currencyProperty.value
                                    .expectJsonPrimitive(currencyProperty.key)
                                    .expectJsonNumberParseLong(currencyProperty.key)
                            )
                        },
                        rewards = run {
                            offerObj
                                .expectJsonProperty("Rewards")
                                .expectJsonArray("Rewards")
                                .mapTo(persistentListOf<FeaturedBundleStore.Reward>().builder()) { element ->
                                    val itemRewardsObj = element.expectJsonObjectAsJsonArrayElement("Rewards")
                                        FeaturedBundleStore.Reward(
                                            itemType = run {
                                                val itemTypeID = itemRewardsObj
                                                    .expectJsonProperty("ItemTypeID")
                                                    .expectJsonPrimitive("ItemTypeID")
                                                    .expectNonBlankJsonString("ItemTypeID")
                                                    .content
                                                ItemType.OBJECTS.find { it.id == itemTypeID }
                                                    ?: ItemType.Other("", itemTypeID)
                                            },
                                            itemID = run {
                                                itemRewardsObj
                                                    .expectJsonProperty("ItemID")
                                                    .expectJsonPrimitive("ItemID")
                                                    .expectNonBlankJsonString("ItemID")
                                                    .content
                                            },
                                            quantity = run {
                                                itemRewardsObj
                                                    .expectJsonProperty("Quantity")
                                                    .expectJsonPrimitive("Quantity")
                                                    .expectJsonNumberParseLong("Quantity")
                                            }
                                        )
                                }
                                .build()

                        },
                    )
                }?.build()
            ,
            totalBaseCost = run {
                bundleObj["TotalBaseCost"]
                    ?.jsonNullable()
                    ?.expectJsonObject("TotalBaseCost")
                    ?.entries
                    ?.also {
                        when {
                            it.isEmpty() -> unexpectedResponseError(
                                "FeaturedBundle TotalBaseCost have no currency present"
                            )
                            it.size > 1 -> unexpectedResponseError(
                                "FeaturedBundle TotalBaseCost have more than 1 currency present"
                            )
                        }
                    }
                    ?.first()
                    ?.let { currencyEntry ->
                        StoreCost(
                            currency = StoreCurrency.ofID(currencyEntry.key),
                            amount = currencyEntry.value
                                .expectJsonPrimitive(currencyEntry.key)
                                .expectJsonNumberParseLong(currencyEntry.key)
                        )
                    }
            },
            totalDiscountedCost = run {
                bundleObj["TotalDiscountedCost"]
                    ?.jsonNullable()
                    ?.expectJsonObject("TotalDiscountedCost")
                    ?.entries
                    ?.also {
                        when {
                            it.isEmpty() -> unexpectedResponseError(
                                "FeaturedBundle TotalDiscountedCost have no currency present"
                            )
                            it.size > 1 -> unexpectedResponseError(
                                "FeaturedBundle TotalDiscountedCost have more than 1 currency present"
                            )
                        }
                    }
                    ?.first()
                    ?.let { currencyEntry ->
                        StoreCost(
                            currency = StoreCurrency.ofID(currencyEntry.key),
                            amount = currencyEntry.value
                                .expectJsonPrimitive(currencyEntry.key)
                                .expectJsonNumberParseLong(currencyEntry.key)
                        )
                    }
            },
            totalDiscountPercent = run {
                bundleObj
                    .expectJsonProperty("TotalDiscountPercent")
                    .expectJsonPrimitive("TotalDiscountPercent")
                    .expectJsonNumberParseFloat("TotalDiscountPercent")

            },
            durationRemaining = run {
                bundleObj
                    .expectJsonProperty("DurationRemainingInSeconds")
                    .expectJsonPrimitive("DurationRemainingInSeconds")
                    .expectJsonNumberParseInt("DurationRemainingInSeconds")
                    .seconds
            },
            wholesaleOnly = run {
                bundleObj
                    .expectJsonProperty("WholesaleOnly")
                    .expectJsonPrimitive("WholesaleOnly")
                    .expectJsonBooleanParseBoolean("WholesaleOnly")
            }
        )
    }

    private fun parseSkinsPanelData(
        obj: JsonObject
    ): SkinsPanelStore {
        val skinPanelObj = obj["SkinsPanelLayout"]
            ?: return SkinsPanelStore(
                false,
                Result.success(null)
            )
        return SkinsPanelStore(
            open = true,
            offer = runCatching {
                parseSkinsPanelOffer(skinPanelObj.expectJsonObject("SkinPanelLayout"))
            }
        )
    }

    private fun parseSkinsPanelOffer(
        obj: JsonObject
    ): SkinsPanelStore.Offer {

        return SkinsPanelStore.Offer(
            offeredItemIds = run {
                obj
                    .expectJsonProperty("SingleItemOffers")
                    .expectJsonArray("SingleItemOffers")
                    .mapTo(persistentListOf<String>().builder()) { element ->
                        element
                            .expectJsonPrimitiveAsArrayElement("SingleItemOffers[]")
                            .expectNonBlankJsonString("SingleItemOffers[]")
                            .content
                    }
                    .build()
            },
            itemOffers = run {
                obj
                    .expectJsonProperty("SingleItemStoreOffers")
                    .expectJsonArray("SingleItemStoreOffers")
                    .associateTo(persistentMapOf<String, SkinsPanelStore.ItemOffer>().builder()) { element ->
                        val elementObj = element
                            .expectJsonObjectAsJsonArrayElement("SingleItemStoreOffers[]")
                        val offerID = elementObj
                            .expectJsonProperty("OfferID")
                            .expectJsonPrimitive("SingleItemStoreOffers[];OfferID")
                            .expectNonBlankJsonString("SingleItemStoreOffers[];OfferID")
                            .content
                        offerID to SkinsPanelStore.ItemOffer(
                            offerId = offerID,
                            isDirectPurchase = run {
                                elementObj
                                    .expectJsonProperty("IsDirectPurchase")
                                    .expectJsonPrimitive("SingleItemStoreOffers[];IsDirectPurchase")
                                    .expectJsonBooleanParseBoolean("SingleItemStoreOffers[];IsDirectPurchase")
                            },
                            startDate = run {
                                elementObj
                                    .expectJsonProperty("StartDate")
                                    .expectJsonPrimitive("SingleItemStoreOffers[];StartDate")
                                    .expectNonBlankJsonString("SingleItemStoreOffers[];StartDate")
                                    .content
                                    .let { ISO8601.fromISOString(it) }
                            },
                            cost = run {
                                elementObj
                                    .expectJsonProperty("Cost")
                                    .expectJsonObject("SingleItemStoreOffers[];Cost")
                                    .entries
                                    .also {
                                        when {
                                            it.isEmpty() -> unexpectedResponseError(
                                                "SkinsPanelOffer;SingleItemStoreOffers;Cost have no currency present"
                                            )
                                            it.size > 1 -> unexpectedResponseError(
                                                "SkinsPanelOffer;SingleItemStoreOffers;Cost have more than 1 currency present"
                                            )
                                        }
                                    }
                                    .first()
                                    .let { currencyEntry ->
                                        StoreCost(
                                            currency = StoreCurrency.ofID(currencyEntry.key),
                                            amount = currencyEntry.value
                                                .expectJsonPrimitive(currencyEntry.key)
                                                .expectJsonNumberParseLong(currencyEntry.key)
                                        )
                                    }
                            },
                            rewards = run {
                                elementObj
                                    .expectJsonProperty("Rewards")
                                    .expectJsonArray("SkinsPanelOffer;SingleItemStoreOffers;Rewards")
                                    .mapTo(persistentListOf<SkinsPanelStore.Reward>().builder()) { element ->
                                        val itemRewardsObj = element
                                            .expectJsonObjectAsJsonArrayElement("SkinsPanelOffer;SingleItemStoreOffers;Rewards")
                                            SkinsPanelStore.Reward(
                                            itemType = run {
                                                val itemTypeID = itemRewardsObj
                                                    .expectJsonProperty("ItemTypeID")
                                                    .expectJsonPrimitive("SkinsPanelOffer;SingleItemStoreOffers;Rewards;ItemTypeID")
                                                    .expectNonBlankJsonString("SkinsPanelOffer;SingleItemStoreOffers;Rewards;ItemTypeID")
                                                    .content
                                                ItemType.OBJECTS.find { it.id == itemTypeID }
                                                    ?: ItemType.Other("", itemTypeID)
                                            },
                                            itemID = run {
                                                itemRewardsObj
                                                    .expectJsonProperty("ItemID")
                                                    .expectJsonPrimitive("SkinsPanelOffer;SingleItemStoreOffers;Rewards;ItemID")
                                                    .expectNonBlankJsonString("SkinsPanelOffer;SingleItemStoreOffers;Rewards;ItemID")
                                                    .content
                                            },
                                            quantity = run {
                                                itemRewardsObj
                                                    .expectJsonProperty("Quantity")
                                                    .expectJsonPrimitive("SkinsPanelOffer;SingleItemStoreOffers;Rewards;Quantity")
                                                    .expectJsonNumberParseLong("SkinsPanelOffer;SingleItemStoreOffers;Rewards;Quantity")
                                            }
                                        )
                                    }
                                    .build()
                            }
                        )
                    }
                    .build()
            },
            remainingDuration = run {
                obj
                    .expectJsonProperty("SingleItemOffersRemainingDurationInSeconds")
                    .expectJsonPrimitive("SkinsPanelOffer;SingleItemOffersRemainingDurationInSeconds")
                    .expectJsonNumberParseLong("SkinsPanelOffer;SingleItemOffersRemainingDurationInSeconds")
                    .seconds
            }
        )
    }

    private fun parseUpgradeCurrencyStore(
        obj: JsonObject
    ): UpgradeCurrencyStore {
        val upgradeCurrencyStoreObj = obj["UpgradeCurrencyStore"]
            ?: return UpgradeCurrencyStore(
                open = false,
                offers = Result.success(null)
            )
        return UpgradeCurrencyStore(
            open = true,
            offers = runCatching {
                parseUpgradeCurrencyStoreOffer(upgradeCurrencyStoreObj.expectJsonObject("UpgradeCurrencyStore"))
            }
        )
    }

    private fun parseUpgradeCurrencyStoreOffer(
        obj: JsonObject
    ): UpgradeCurrencyStore.Offer {

        return UpgradeCurrencyStore.Offer(
            offers = run {
                obj
                    .expectJsonProperty("UpgradeCurrencyOffers")
                    .expectJsonArray("UpgradeCurrencyStore;UpgradeCurrencyOffers")
                    .mapTo(persistentListOf<UpgradeCurrencyStore.ItemOffer>().builder()) { element ->

                        val offerObj = element
                            .expectJsonObjectAsJsonArrayElement("UpgradeCurrencyStore;UpgradeCurrencyOffers[]")

                        val itemOfferObj = offerObj
                            .expectJsonProperty("Offer")
                            .expectJsonObject("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer")

                        UpgradeCurrencyStore.ItemOffer(
                            id = run {
                                offerObj
                                    .expectJsonProperty("OfferID")
                                    .expectJsonPrimitive("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;OfferID")
                                    .expectNonBlankJsonString("UpgradeCurrencyStore;UpgradeCurrencyOffers;OfferID")
                                    .content
                            },
                            storeFrontItemID = run {
                                offerObj
                                    .expectJsonProperty("StoreFrontItemID")
                                    .expectJsonPrimitive("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;StoreFrontItemID")
                                    .expectNonBlankJsonString("UpgradeCurrencyStore;UpgradeCurrencyOffers;StoreFrontItemID")
                                    .content
                            },
                            isDirectPurchase = run {
                                itemOfferObj
                                    .expectJsonProperty("IsDirectPurchase")
                                    .expectJsonPrimitive("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;IsDirectPurchase")
                                    .expectJsonBooleanParseBoolean("UpgradeCurrencyStore;UpgradeCurrencyOffers;IsDirectPurchase")
                            },
                            startDate = run {
                                itemOfferObj
                                    .expectJsonProperty("StartDate")
                                    .expectJsonPrimitive("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;StartDate")
                                    .expectNonBlankJsonString("UpgradeCurrencyStore;UpgradeCurrencyOffers;StartDate")
                                    .content
                                    .let { ISO8601.fromISOString(it) }
                            },
                            cost = run {
                                itemOfferObj
                                    .expectJsonProperty("Cost")
                                    .expectJsonObject("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Cost")
                                    .entries
                                    .also {
                                        when {
                                            it.isEmpty() -> unexpectedResponseError(
                                                "UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Cost have no currency present"
                                            )
                                            it.size > 1 -> unexpectedResponseError(
                                                "UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Cost have more than 1 currency present"
                                            )
                                        }
                                    }
                                    .first()
                                    .let { currencyEntry ->
                                        StoreCost(
                                            currency = StoreCurrency.ofID(currencyEntry.key),
                                            amount = currencyEntry.value
                                                .expectJsonPrimitive(currencyEntry.key)
                                                .expectJsonNumberParseLong(currencyEntry.key)
                                        )
                                    }
                            },
                            rewards = run {
                                itemOfferObj
                                    .expectJsonProperty("Rewards")
                                    .expectJsonArray("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards")
                                    .mapTo(persistentListOf<UpgradeCurrencyStore.Reward>().builder()) { element ->
                                        val itemRewardsObj = element
                                            .expectJsonObjectAsJsonArrayElement("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards")
                                        UpgradeCurrencyStore.Reward(
                                            itemType = run {
                                                val itemTypeID = itemRewardsObj
                                                    .expectJsonProperty("ItemTypeID")
                                                    .expectJsonPrimitive("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards;ItemTypeID")
                                                    .expectNonBlankJsonString("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards;ItemTypeID")
                                                    .content
                                                ItemType.OBJECTS.find { it.id == itemTypeID }
                                                    ?: ItemType.Other("", itemTypeID)
                                            },
                                            itemID = run {
                                                itemRewardsObj
                                                    .expectJsonProperty("ItemID")
                                                    .expectJsonPrimitive("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards;ItemID")
                                                    .expectNonBlankJsonString("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards;ItemID")
                                                    .content
                                            },
                                            quantity = run {
                                                itemRewardsObj
                                                    .expectJsonProperty("Quantity")
                                                    .expectJsonPrimitive("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards;Quantity")
                                                    .expectJsonNumberParseLong("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards;Quantity")
                                            }
                                        )
                                    }
                                    .build()
                            }
                        )
                    }
                    .build()
            }
        )
    }

    private fun parseAccessoryStore(
        obj: JsonObject
    ): AccessoryStore {
        val accessoryStoreObj = obj["AccessoryStore"]
            ?: return AccessoryStore(
                open = false,
                offer = Result.success(null)
            )
        return AccessoryStore(
            open = true,
            offer = runCatching {
                parseAccessoryStoreOffer(accessoryStoreObj.expectJsonObject("AccessoryStore"))
            }
        )
    }

    private fun parseAccessoryStoreOffer(
        obj: JsonObject
    ): AccessoryStore.Offer {

        return AccessoryStore.Offer(
            storeFrontId = run {
                obj
                    .expectJsonProperty("StorefrontID")
                    .expectJsonPrimitive("AccessoryStore;StorefrontID")
                    .expectNonBlankJsonString("AccessoryStore;StorefrontID")
                    .content
            },
            offers = run {
                obj
                    .expectJsonProperty("AccessoryStoreOffers")
                    .expectJsonArray("AccessoryStore;AccessoryStoreOffers")
                    .mapTo(persistentListOf<AccessoryStore.ItemOffer>().builder()) { element ->

                        val offerObj = element
                            .expectJsonObjectAsJsonArrayElement("AccessoryStoreOffers[]")

                        val itemOfferObj = offerObj
                            .expectJsonProperty("Offer")
                            .expectJsonObject("AccessoryStore;AccessoryStoreOffers[];Offer")

                        AccessoryStore.ItemOffer(
                            id = run {
                                itemOfferObj
                                    .expectJsonProperty("OfferID")
                                    .expectJsonPrimitive("AccessoryStore;AccessoryStoreOffers[];Offer;OfferID")
                                    .expectNonBlankJsonString("AccessoryStore;AccessoryStoreOffers[];OfferID")
                                    .content
                            },
                            isDirectPurchase = run {
                                itemOfferObj
                                    .expectJsonProperty("IsDirectPurchase")
                                    .expectJsonPrimitive("AccessoryStore;AccessoryStoreOffers[];Offer;IsDirectPurchase")
                                    .expectJsonBooleanParseBoolean("AccessoryStore;AccessoryStoreOffers[];IsDirectPurchase")
                            },
                            startDate = run {
                                itemOfferObj
                                    .expectJsonProperty("StartDate")
                                    .expectJsonPrimitive("AccessoryStore;AccessoryStoreOffers[];Offer;StartDate")
                                    .expectNonBlankJsonString("AccessoryStore;AccessoryStoreOffers[];StartDate")
                                    .content
                                    .let { ISO8601.fromISOString(it) }
                            },
                            cost = run {
                                itemOfferObj
                                    .expectJsonProperty("Cost")
                                    .expectJsonObject("AccessoryStore;AccessoryStoreOffers[];Offer;Cost")
                                    .entries
                                    .also {
                                        when {
                                            it.isEmpty() -> unexpectedResponseError(
                                                "AccessoryStore;AccessoryStoreOffers[];Offer;Cost have no currency present"
                                            )
                                            it.size > 1 -> unexpectedResponseError(
                                                "AccessoryStore;AccessoryStoreOffers[];Offer;Cost have more than 1 currency present"
                                            )
                                        }
                                    }
                                    .first()
                                    .let { currencyEntry ->
                                        StoreCost(
                                            currency = StoreCurrency.ofID(currencyEntry.key),
                                            amount = currencyEntry.value
                                                .expectJsonPrimitive(currencyEntry.key)
                                                .expectJsonNumberParseLong(currencyEntry.key)
                                        )
                                    }
                            },
                            rewards = run {
                                itemOfferObj
                                    .expectJsonProperty("Rewards")
                                    .expectJsonArray("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards")
                                    .mapTo(persistentListOf<AccessoryStore.Reward>().builder()) { element ->
                                        val itemRewardsObj = element
                                            .expectJsonObjectAsJsonArrayElement("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards")
                                        AccessoryStore.Reward(
                                            itemType = run {
                                                val itemTypeID = itemRewardsObj
                                                    .expectJsonProperty("ItemTypeID")
                                                    .expectJsonPrimitive("AccessoryStore;AccessoryStoreOffers[];;Offer;Rewards;ItemTypeID")
                                                    .expectNonBlankJsonString("AccessoryStore;AccessoryStoreOffers[];;Offer;Rewards;ItemTypeID")
                                                    .content
                                                ItemType.OBJECTS.find { it.id == itemTypeID }
                                                    ?: ItemType.Other("", itemTypeID)
                                            },
                                            itemID = run {
                                                itemRewardsObj
                                                    .expectJsonProperty("ItemID")
                                                    .expectJsonPrimitive("AccessoryStore;AccessoryStoreOffers[];Offer;Rewards;ItemID")
                                                    .expectNonBlankJsonString("AccessoryStore;AccessoryStoreOffers[];Offer;Rewards;ItemID")
                                                    .content
                                            },
                                            quantity = run {
                                                itemRewardsObj
                                                    .expectJsonProperty("Quantity")
                                                    .expectJsonPrimitive("AccessoryStore;AccessoryStoreOffers[];Offer;Rewards;Quantity")
                                                    .expectJsonNumberParseLong("AccessoryStore;AccessoryStoreOffers[];Offer;Rewards;Quantity")
                                            }
                                        )
                                    }
                                    .build()
                            },
                            contractID = run {
                                offerObj
                                    .expectJsonProperty("ContractID")
                                    .expectJsonPrimitive("AccessoryStore;AccessoryStoreOffers[];ContractID")
                                    .expectNonBlankJsonString("AccessoryStore;AccessoryStoreOffers[];ContractID")
                                    .content
                            }
                        )
                    }
                    .associateByTo(persistentMapOf<String, AccessoryStore.ItemOffer>().builder()) { it.id }
                    .build()
            },
            remainingDuration = run {
                obj
                    .expectJsonProperty("AccessoryStoreRemainingDurationInSeconds")
                    .expectJsonPrimitive("AccessoryStore;AccessoryStoreRemainingDurationInSeconds")
                    .expectJsonNumberParseInt("AccessoryStore;AccessoryStoreRemainingDurationInSeconds")
                    .seconds
            }
        )
    }

    private fun parseBonusStore(
        obj: JsonObject
    ): BonusStore {
        val bonusStoreObj = obj["BonusStore"]
            ?: return BonusStore(
                open = false,
                offer = Result.success(null)
            )
        return BonusStore(
            open = true,
            offer = runCatching {
                parseBonusStoreOffer(bonusStoreObj.expectJsonObject("BonusStore"))
            }
        )
    }

    private fun parseBonusStoreOffer(
        obj: JsonObject
    ): BonusStore.Offer {

        return BonusStore.Offer(
            offers = run {
                obj
                    .expectJsonProperty("BonusStoreOffers")
                    .expectJsonArray("BonusStore;BonusStoreOffers")
                    .mapTo(persistentListOf<BonusStore.ItemOffer>().builder()) { element ->
                        val elementObj = element
                            .expectJsonObjectAsJsonArrayElement("BonusStore;BonusStoreOffers[]")
                        val offerObj = elementObj
                            .expectJsonProperty("Offer")
                            .expectJsonObject("Offer")
                        BonusStore.ItemOffer(
                            bonusOfferID = run {
                                elementObj
                                    .expectJsonProperty("BonusOfferID")
                                    .expectJsonPrimitive("BonusStore;BonusOfferID")
                                    .expectNonBlankJsonString("BonusStore;BonusOfferID")
                                    .content
                            },
                            offerID = run {
                                offerObj
                                    .expectJsonProperty("OfferID")
                                    .expectJsonPrimitive("BonusStore;BonusStoreOffers[];OfferID")
                                    .expectNonBlankJsonString("BonusStore;BonusStoreOffers[];OfferID")
                                    .content
                            },
                            isDirectPurchase = run {
                                offerObj
                                    .expectJsonProperty("IsDirectPurchase")
                                    .expectJsonPrimitive("BonusStore;BonusStoreOffers[];IsDirectPurchase")
                                    .expectJsonBooleanParseBoolean("BonusStore;BonusStoreOffers[];IsDirectPurchase")
                            },
                            startDate = run {
                                offerObj
                                    .expectJsonProperty("StartDate")
                                    .expectJsonPrimitive("BonusStore;BonusStoreOffers[];StartDate")
                                    .expectNonBlankJsonString("BonusStore;BonusStoreOffers[];StartDate")
                                    .content
                                    .let { ISO8601.fromISOString(it) }
                            },
                            cost = run {
                                offerObj
                                    .expectJsonProperty("Cost")
                                    .expectJsonObject("BonusStore;BonusStoreOffers[];Cost")
                                    .entries
                                    .also {
                                        when {
                                            it.isEmpty() -> unexpectedResponseError(
                                                "BonusStore;BonusStoreOffers[];Cost have no currency present"
                                            )
                                            it.size > 1 -> unexpectedResponseError(
                                                "BonusStore;BonusStoreOffers[];Cost have more than 1 currency present"
                                            )
                                        }
                                    }
                                    .first()
                                    .let { currencyEntry ->
                                        StoreCost(
                                            currency = StoreCurrency.ofID(currencyEntry.key),
                                            amount = currencyEntry.value
                                                .expectJsonPrimitive(currencyEntry.key)
                                                .expectJsonNumberParseLong(currencyEntry.key)
                                        )
                                    }
                            },
                            rewards = run {
                                offerObj
                                    .expectJsonProperty("Rewards")
                                    .expectJsonArray("BonusStore;BonusStoreOffers[];Rewards[]")
                                    .associateTo(
                                        persistentMapOf<String, BonusStore.ItemOfferReward>()
                                            .builder(),
                                        transform = { itemRewardsObj ->
                                            itemRewardsObj.expectJsonObject("BonusStore;BonusStoreOffers[];Rewards[];_")
                                            val itemId = itemRewardsObj
                                                .expectJsonProperty("ItemID")
                                                .expectJsonPrimitive("BonusStore;BonusStoreOffers[];Rewards;ItemID")
                                                .expectNonBlankJsonString("BonusStore;BonusStoreOffers[];Rewards;ItemID")
                                                .content
                                            itemId to BonusStore.ItemOfferReward(
                                                itemType = run {
                                                    val itemTypeID = itemRewardsObj
                                                        .expectJsonProperty("ItemTypeID")
                                                        .expectJsonPrimitive("BonusStore;BonusStoreOffers[];Rewards;ItemTypeID")
                                                        .expectNonBlankJsonString("BonusStore;BonusStoreOffers[];Rewards;ItemTypeID")
                                                        .content
                                                    ItemType.OBJECTS.find { it.id == itemTypeID }
                                                        ?: ItemType.Other("", itemTypeID)
                                                },
                                                itemID = itemId,
                                                quantity = run {
                                                    itemRewardsObj
                                                        .expectJsonProperty("Quantity")
                                                        .expectJsonPrimitive("BonusStore;BonusStoreOffers[];Rewards;Quantity")
                                                        .expectJsonNumberParseLong("BonusStore;BonusStoreOffers[]s;Rewards;Quantity")
                                                }
                                            )
                                        }
                                    )
                                    .build()
                            },
                            discountPercent = run {
                                elementObj
                                    .expectJsonProperty("DiscountPercent")
                                    .expectJsonPrimitive("BonusStore;BonusStoreOffers[];DiscountPercent")
                                    .expectJsonNumberParseFloat("BonusStore;BonusStoreOffers[];DiscountPercent")
                                    .toInt()
                            },
                            discountedCost = run {
                                elementObj
                                    .expectJsonProperty("DiscountCosts")
                                    .expectJsonObject("BonusStore;BonusStoreOffers[];DiscountCosts")
                                    .entries
                                    .also {
                                        when {
                                            it.isEmpty() -> unexpectedResponseError(
                                                "BonusStore;BonusStoreOffers[];DiscountCosts have no currency present"
                                            )
                                            it.size > 1 -> unexpectedResponseError(
                                                "BonusStore;BonusStoreOffers[];DiscountCosts have more than 1 currency present"
                                            )
                                        }
                                    }
                                    .first()
                                    .let { currencyEntry ->
                                        StoreCost(
                                            currency = StoreCurrency.ofID(currencyEntry.key),
                                            amount = currencyEntry.value
                                                .expectJsonPrimitive("BonusStore;BonusStoreOffers[];" + currencyEntry.key)
                                                .expectJsonNumberParseLong("BonusStore;BonusStoreOffers[];" + currencyEntry.key)
                                        )
                                    }
                            },
                            isSeen = run {
                                elementObj
                                    .expectJsonProperty("IsSeen")
                                    .expectJsonPrimitive("BonusStore;BonusStoreOffers[];IsSeen")
                                    .expectJsonBooleanParseBoolean("BonusStore;BonusStoreOffers[];IsSeen")
                            }
                        )
                    }
                    .build()
            },
            remainingDuration = run {
                obj
                    .expectJsonProperty("BonusStoreRemainingDurationInSeconds")
                    .expectJsonPrimitive("BonusStore;BonusStoreRemainingDurationInSeconds")
                    .expectJsonNumberParseInt("BonusStore;BonusStoreRemainingDurationInSeconds")
                    .seconds
            }
        )
    }
}