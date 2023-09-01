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
            unexpectedResponseError("UNABLE TO PARSE STOREFRONT RESPONSE BODY, UNHANDLED HTTP STATUS CODE (${response.statusCode})")
        }
    }

    private fun parseStoreFrontResponseBody(
        response: JsonHttpResponse
    ): Result<StoreFrontData> {
        return runCatching {
            val body = response.body.getOrThrow()
            val obj = body.expectJsonObject("StoreFrontResponseBody")

            StoreFrontData(
                featuredBundle = parseFeaturedBundle(obj),
                skinsPanel = parseSkinsPanelData(obj),
                upgradeCurrencyStore = parseUpgradeCurrencyStore(obj),
                bonusStore = parseBonusStore(obj),
                accessoryStore = parseAccessoryStore(obj)
            )
        }
    }

    private fun parseFeaturedBundle(
        obj: JsonObject
    ): FeaturedBundle {

        val offer = obj["FeaturedBundle"]

        return FeaturedBundle(
            open = offer != null,
            offer = runCatching {
                offer?.let {
                    FeaturedBundle.Offer(
                        bundle = run {
                            val bundleObj = offer.expectJsonObject("Bundle")
                            parseFeaturedBundleBundle(bundleObj)
                        },
                        bundles = run {
                            val bundlesArray = offer.expectJsonArray("Bundles")
                            bundlesArray
                                .mapTo(persistentListOf<FeaturedBundle.Bundle>().builder()) { element ->
                                    val bundleObj = element
                                        .expectJsonObjectAsJsonArrayElement("Bundles")
                                    parseFeaturedBundleBundle(bundleObj)
                                }
                                .build()
                        },
                        bundleRemainingDuration = run {
                            offer
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
    ): FeaturedBundle.Bundle {
        return FeaturedBundle.Bundle(
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
                .expectJsonArray("Items")
                .mapTo(persistentListOf<FeaturedBundle.ItemBaseOffer>().builder()) { item ->
                    val itemObj = item
                        .expectJsonObjectAsJsonArrayElement("Items")
                    val itemOfferObj = itemObj
                        .expectJsonProperty("Item")
                        .expectJsonObject("Item")
                    FeaturedBundle.ItemBaseOffer(
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
                            FeaturedBundle.Reward(
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
                                        .expectJsonProperty("Quantity")
                                        .expectJsonPrimitive("Quantity")
                                        .expectJsonNumberParseLong("Quantity")
                                }
                            )
                        }
                    )

                }
                .build(),
            itemDiscountedOffers = bundleObj["ItemOffers"]
                ?.expectJsonArray("ItemOffers")
                ?.mapTo(persistentListOf<FeaturedBundle.ItemDiscountedOffer>().builder()) { element ->
                    val itemObj = element
                        .expectJsonObjectAsJsonArrayElement("ItemOffers")
                    val offerObj = itemObj
                        .expectJsonProperty("Offer")
                        .expectJsonObject("Offer")
                    FeaturedBundle.ItemDiscountedOffer(
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
                        reward = run {
                            val itemRewardsObj = offerObj
                                .expectJsonProperty("Rewards")
                                .expectJsonObject("Rewards")
                            FeaturedBundle.Reward(
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
                        },
                    )
                }?.build()
            ,
            totalBaseCost = run {
                bundleObj["TotalBaseCost"]
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
    ): SkinsPanelData {
        val skinPanelObj = obj["SkinPanelLayout"]
            ?.expectJsonObject("SkinPanelLayout")
            ?: return SkinsPanelData(
                false,
                Result.success(null)
            )
        return SkinsPanelData(
            open = true,
            offer = runCatching { parseSkinsPanelOffer(skinPanelObj) }
        )
    }

    private fun parseSkinsPanelOffer(
        obj: JsonObject
    ): SkinsPanelData.Offer {

        return SkinsPanelData.Offer(
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
                    .associateTo(persistentMapOf<String, SkinsPanelData.ItemOffer>().builder()) { element ->
                        val elementObj = element
                            .expectJsonObjectAsJsonArrayElement("SingleItemStoreOffers[]")
                        val offerID = elementObj
                            .expectJsonProperty("OfferID")
                            .expectJsonPrimitive("SingleItemStoreOffers[];OfferID")
                            .expectNonBlankJsonString("SingleItemStoreOffers[];OfferID")
                            .content
                        offerID to SkinsPanelData.ItemOffer(
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
                            reward = run {
                                val itemRewardsObj = elementObj
                                    .expectJsonProperty("Rewards")
                                    .expectJsonObject("SkinsPanelOffer;SingleItemStoreOffers;Rewards")
                                SkinsPanelData.Reward(
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
    ): UpgradeCurrencyStoreData {
        val upgradeCurrencyStoreObj = obj["UpgradeCurrencyStore"]
            ?.expectJsonObject("UpgradeCurrencyStore")
            ?: return UpgradeCurrencyStoreData(
                open = false,
                offers = Result.success(null)
            )
        return UpgradeCurrencyStoreData(
            open = true,
            offers = runCatching { parseUpgradeCurrencyStoreOffer(upgradeCurrencyStoreObj) }
        )
    }

    private fun parseUpgradeCurrencyStoreOffer(
        obj: JsonObject
    ): UpgradeCurrencyStoreData.Offer {

        return UpgradeCurrencyStoreData.Offer(
            offers = run {
                obj
                    .expectJsonProperty("UpgradeCurrencyOffers")
                    .expectJsonArray("UpgradeCurrencyStore;UpgradeCurrencyOffers")
                    .mapTo(persistentListOf<UpgradeCurrencyStoreData.ItemOffer>().builder()) { element ->

                        val offerObj = element
                            .expectJsonObjectAsJsonArrayElement("UpgradeCurrencyStore;UpgradeCurrencyOffers[]")

                        val itemOfferObj = offerObj
                            .expectJsonProperty("Offer")
                            .expectJsonObject("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer")

                        UpgradeCurrencyStoreData.ItemOffer(
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
                            reward = run {
                                val itemRewardsObj = itemOfferObj
                                    .expectJsonProperty("Rewards")
                                    .expectJsonObject("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards")
                                UpgradeCurrencyStoreData.Reward(
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
                        )
                    }
                    .build()
            }
        )
    }

    private fun parseAccessoryStore(
        obj: JsonObject
    ): AccessoryStoreData {
        val accessoryStoreObj = obj["AccessoryStore"]
            ?.expectJsonObject("AccessoryStore")
            ?: return AccessoryStoreData(
                open = false,
                offer = Result.success(null)
            )
        return AccessoryStoreData(
            open = true,
            offer = runCatching { parseAccessoryStoreOffer(accessoryStoreObj) }
        )
    }

    private fun parseAccessoryStoreOffer(
        obj: JsonObject
    ): AccessoryStoreData.Offer {

        return AccessoryStoreData.Offer(
            storeFrontId = run {
                obj
                    .expectJsonProperty("StoreFrontID")
                    .expectJsonPrimitive("AccessoryStore;StoreFrontID")
                    .expectNonBlankJsonString("AccessoryStore;StoreFrontID")
                    .content
            },
            offers = run {
                obj
                    .expectJsonProperty("AccessoryStoreOffers")
                    .expectJsonArray("AccessoryStore;AccessoryStoreOffers")
                    .mapTo(persistentListOf<AccessoryStoreData.ItemOffer>().builder()) { element ->

                        val offerObj = element
                            .expectJsonObjectAsJsonArrayElement("AccessoryStoreOffers[]")

                        val itemOfferObj = offerObj
                            .expectJsonProperty("Offer")
                            .expectJsonObject("AccessoryStore;AccessoryStoreOffers[];Offer")

                        AccessoryStoreData.ItemOffer(
                            id = run {
                                offerObj
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
                            reward = run {
                                val itemRewardsObj = itemOfferObj
                                    .expectJsonProperty("Rewards")
                                    .expectJsonObject("UpgradeCurrencyStore;UpgradeCurrencyOffers;Offer;Rewards")
                                AccessoryStoreData.Reward(
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
                            },
                            contractID = run {
                                itemOfferObj
                                    .expectJsonProperty("ContractID")
                                    .expectJsonPrimitive("AccessoryStore;AccessoryStoreOffers[];ContractID")
                                    .expectNonBlankJsonString("AccessoryStore;AccessoryStoreOffers[];ContractID")
                                    .content
                            }
                        )
                    }
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
    ): BonusStoreData {
        val bonusStoreObj = obj["BonusStore"]
            ?.expectJsonObject("BonusStore")
            ?: return BonusStoreData(
                open = false,
                offer = Result.success(null)
            )
        return BonusStoreData(
            open = true,
            offer = runCatching { parseBonusStoreOffer(bonusStoreObj) }
        )
    }

    private fun parseBonusStoreOffer(
        obj: JsonObject
    ): BonusStoreData.Offer {

        return BonusStoreData.Offer(
            offerID = run {
                obj
                    .expectJsonProperty("BonusOfferID")
                    .expectJsonPrimitive("BonusStore;BonusOfferID")
                    .expectNonBlankJsonString("BonusStore;BonusOfferID")
                    .content
            },
            offers = run {
                obj
                    .expectJsonProperty("BonusStoreOffers")
                    .expectJsonArray("BonusStore;BonusStoreOffers")
                    .mapTo(persistentListOf<BonusStoreData.ItemOffer>().builder()) { element ->
                        val elementObj = element
                            .expectJsonObjectAsJsonArrayElement("BonusStore;BonusStoreOffers[]")
                        BonusStoreData.ItemOffer(
                            offerID = run {
                                elementObj
                                    .expectJsonProperty("OfferID")
                                    .expectJsonPrimitive("BonusStore;BonusStoreOffers[];OfferID")
                                    .expectNonBlankJsonString("BonusStore;BonusStoreOffers[];OfferID")
                                    .content
                            },
                            isDirectPurchase = run {
                                elementObj
                                    .expectJsonProperty("IsDirectPurchase")
                                    .expectJsonPrimitive("BonusStore;BonusStoreOffers[];IsDirectPurchase")
                                    .expectJsonBooleanParseBoolean("BonusStore;BonusStoreOffers[];IsDirectPurchase")
                            },
                            startDate = run {
                                elementObj
                                    .expectJsonProperty("StartDate")
                                    .expectJsonPrimitive("BonusStore;BonusStoreOffers[];StartDate")
                                    .expectNonBlankJsonString("BonusStore;BonusStoreOffers[];StartDate")
                                    .content
                                    .let { ISO8601.fromISOString(it) }
                            },
                            cost = run {
                                elementObj
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
                            reward = run {
                                val itemRewardsObj = elementObj
                                    .expectJsonProperty("Rewards")
                                    .expectJsonObject("BonusStore;BonusStoreOffers[];Rewards")
                                BonusStoreData.ItemOfferReward(
                                    itemType = run {
                                        val itemTypeID = itemRewardsObj
                                            .expectJsonProperty("ItemTypeID")
                                            .expectJsonPrimitive("BonusStore;BonusStoreOffers[];Rewards;ItemTypeID")
                                            .expectNonBlankJsonString("BonusStore;BonusStoreOffers[];Rewards;ItemTypeID")
                                            .content
                                        ItemType.OBJECTS.find { it.id == itemTypeID }
                                            ?: ItemType.Other("", itemTypeID)
                                    },
                                    itemID = run {
                                        itemRewardsObj
                                            .expectJsonProperty("ItemID")
                                            .expectJsonPrimitive("BonusStore;BonusStoreOffers[];Rewards;ItemID")
                                            .expectNonBlankJsonString("BonusStore;BonusStoreOffers[];Rewards;ItemID")
                                            .content
                                    },
                                    quantity = run {
                                        itemRewardsObj
                                            .expectJsonProperty("Quantity")
                                            .expectJsonPrimitive("BonusStore;BonusStoreOffers[];Rewards;Quantity")
                                            .expectJsonNumberParseLong("BonusStore;BonusStoreOffers[]s;Rewards;Quantity")
                                    }
                                )
                            },
                            discountPercent = run {
                                elementObj
                                    .expectJsonProperty("DiscountPercent")
                                    .expectJsonPrimitive("BonusStore;BonusStoreOffers[];DiscountPercent")
                                    .expectJsonNumberParseFloat("BonusStore;BonusStoreOffers[];DiscountPercent")
                            },
                            discountCost = run {
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
                                            currency = StoreCurrency.ofID("BonusStore;BonusStoreOffers[];" + currencyEntry.key),
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