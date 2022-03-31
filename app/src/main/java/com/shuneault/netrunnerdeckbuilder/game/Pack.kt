package com.shuneault.netrunnerdeckbuilder.game

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

/**
 * Created by sebast on 02/07/16.
 */
class Pack: Serializable {
    object SetCode {
        const val CORE_SET = "core"
        const val REVISED_CORE_SET = "core2"
        const val SYSTEM_CORE_2019 = "sc19"
    }

    var code: String? = null
    var cycleCode: String? = null
    var dateRelease: String? = null
        private set
    var name: String? = null
    var position = 0
        private set
    var size = 0
        private set
    val cardLinks = ArrayList<CardLink>()

    constructor() {}
    constructor(json: JSONObject) {
        this.code = json.optString(KEY_CODE, "")
        cycleCode = json.optString(KEY_CYCLE_CODE, "")
        dateRelease = json.optString(KEY_DATE_RELEASE, "")
        name = json.optString(KEY_NAME, "")
        position = json.optInt(KEY_POSITION, 0)
        size = json.optInt(KEY_SIZE, 0)
        val cards = json.optJSONArray("cards")
        if (cards != null) {
            for (i in 0 until cards.length()) {
                try {
                    var card: JSONObject
                    card = cards.getJSONObject(i)
                    val cardCode = card.getString("code")
                    val quantity = card.getInt("quantity")
                    val cl = CardLink(cardCode, quantity)
                    cardLinks.add(cl)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    val isCoreSet: Boolean
        get() = code == SetCode.CORE_SET || code == SetCode.REVISED_CORE_SET || code == SetCode.SYSTEM_CORE_2019

    companion object {
        const val KEY_CODE = "code"
        const val KEY_CYCLE_CODE = "cycle_code"
        const val KEY_DATE_RELEASE = "date_release"
        const val KEY_NAME = "name"
        const val KEY_POSITION = "position"
        const val KEY_SIZE = "size"
    }
}