package com.shuneault.netrunnerdeckbuilder.game

import org.json.JSONException
import org.json.JSONObject

class CardMWL(mwlData: JSONObject) {
    var globalPenalty = 0
    var universalFactionCost = 0
    private var hasDeckLimit = false
    var deckLimit = -1
    var isRestricted = false
    fun hasDeckLimit(): Boolean {
        return hasDeckLimit
    }

    val isRemoved: Boolean
        get() = hasDeckLimit && deckLimit == 0

    init {
        try {
            if (mwlData.has("global_penalty")) {
                val globalPenalties = mwlData.getInt("global_penalty")
                globalPenalty = globalPenalties
            }
            if (mwlData.has("universal_faction_cost")) {
                universalFactionCost = mwlData.getInt("universal_faction_cost")
            }
            if (mwlData.has("deck_limit")) {
                hasDeckLimit = true
                deckLimit = mwlData.getInt("deck_limit")
            }
            if (mwlData.has("is_restricted")) {
                isRestricted = mwlData.getInt("is_restricted") == 1
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}