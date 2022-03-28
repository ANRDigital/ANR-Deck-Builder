package com.shuneault.netrunnerdeckbuilder.game

import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL

class CardBuilder {
    private var imageUrlTemplate: String
    private var code: String? = null
    private var setCode: String? = null
    private var quantity = "3"
    private var typeCode = ""
    private var title: String? = null

    constructor() {
        imageUrlTemplate = ""
    }

    constructor(imageUrlTemplate: String) {
        this.imageUrlTemplate = imageUrlTemplate
    }

    fun BuildFromJson(json: JSONObject): Card? {
        var card: Card? = null
        try {
            card = Card()
            card.code = json.optString(NAME_CODE)
            card.cost = json.optString(NAME_COST)
            card.title = json.optString(NAME_TITLE)
            card.typeCode = json.optString(NAME_TYPE_CODE)
            card.subtype = json.optString(NAME_SUBTYPE)
            card.text = json.optString(NAME_TEXT)
            card.baseLink = json.optString(NAME_BASELINK)
            card.factionCode = json.optString(NAME_FACTION_CODE)
            card.factionCost = json.optInt(NAME_FACTION_COST, 0)
            card.flavor = json.optString(NAME_FLAVOR)
            card.illustrator = json.optString(NAME_ILLUSTRATOR)
            card.setInfluenceLimit(json.optString(NAME_INFLUENCE_LIMIT))
            card.setMinimumDeckSize(json.optString(NAME_MINIMUM_DECK_SIZE))
            card.setNumber(json.optString(NAME_NUMBER))
            card.setQuantity(json.optString(NAME_QUANTITY))
            card.deckLimit = json.optInt(NAME_DECK_LIMIT)
            val packCode = json.optString(NAME_SET_CODE)
            card.setCode = packCode
            card.isUniqueness = json.optBoolean(NAME_UNIQUENESS)
            // does it have an unusual image_src? If not follow standard pattern
            var imageUrl = json.optString(NAME_IMAGE_URL_OVERRIDE)
            if (imageUrl == null || imageUrl.length < 1) {
                imageUrl = imageUrlTemplate.replace("{code}", card.code)
            }
            card.imageSrc = URL(imageUrl)
            card.sideCode = json.optString(NAME_SIDE_CODE)
            card.agendaPoints = json.optInt(NAME_AGENDA_POINTS, 0)
            card.advancementCost = json.optInt(NAME_ADVANCEMENT_COST, 0)
            card.memoryUnits = json.optInt(NAME_MEMORY_UNITS, 0)
            card.setTrash(json.optInt(NAME_TRASH, 0))
            card.strength = json.optInt(NAME_STRENGTH, 0)
        } catch (e: MalformedURLException) {
            //
            e.printStackTrace()
        }
        return card
    }

    fun withCode(code: String?): CardBuilder {
        this.code = code
        return this
    }

    // only for test
    fun Build(): Card {
        val c = Card()
        c.code = code
        c.setCode = setCode
        c.setQuantity(quantity)
        if (!typeCode.isEmpty()) c.typeCode = typeCode
        c.title = title
        return c
    }

    fun withSetCode(setCode: String?): CardBuilder {
        this.setCode = setCode
        return this
    }

    fun withQuantity(quantity: Int): CardBuilder {
        this.quantity = quantity.toString()
        return this
    }

    fun withTypeCode(typeCode: String): CardBuilder {
        this.typeCode = typeCode
        return this
    }

    fun withTitle(title: String?): CardBuilder {
        this.title = title
        return this
    }

    companion object {
        //    public static final String NAME_LAST_MODIFIED = "last-modified";
        private const val NAME_CODE = "code"
        private const val NAME_COST = "cost"
        private const val NAME_TITLE = "title"

        //    public static final String NAME_TYPE = "type";
        private const val NAME_TYPE_CODE = "type_code"
        private const val NAME_SUBTYPE = "keywords"

        //    public static final String NAME_SUBTYPE_CODE = "subtype_code";
        private const val NAME_TEXT = "text"
        private const val NAME_BASELINK = "base_link"

        //    public static final String NAME_FACTION = "faction";
        private const val NAME_FACTION_CODE = "faction_code"
        private const val NAME_FACTION_COST = "faction_cost"
        private const val NAME_FLAVOR = "flavor"
        private const val NAME_ILLUSTRATOR = "illustrator"
        private const val NAME_INFLUENCE_LIMIT = "influence_limit"
        private const val NAME_MINIMUM_DECK_SIZE = "minimum_deck_size"
        private const val NAME_NUMBER = "position"
        private const val NAME_QUANTITY = "quantity"
        private const val NAME_DECK_LIMIT = "deck_limit"

        //    public static final String NAME_SET_NAME = "setname";
        private const val NAME_SET_CODE = "pack_code"

        //    public static final String NAME_SIDE = "side";
        private const val NAME_SIDE_CODE = "side_code"
        private const val NAME_UNIQUENESS = "uniqueness"

        //    public static final String NAME_URL = "url";
        private const val NAME_IMAGE_URL_OVERRIDE = "image_url"

        //    public static final String NAME_IMAGE_SRC = "imagesrc";
        private const val NAME_AGENDA_POINTS = "agenda_points"
        private const val NAME_ADVANCEMENT_COST = "advancement_cost"
        private const val NAME_MEMORY_UNITS = "memory_cost"
        private const val NAME_TRASH = "trash_cost"
        private const val NAME_STRENGTH = "strength"
    }
}