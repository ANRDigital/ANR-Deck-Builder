package com.shuneault.netrunnerdeckbuilder.game

import org.json.JSONObject

class Cycle {
    var code: String? = null
    var isRotated = false
        private set

    constructor(json: JSONObject) {
        this.code = json.optString(KEY_CODE, "")
        isRotated = json.optBoolean(KEY_ROTATED, false)
    }

    constructor() {}

    fun setRotation(value: Boolean) {
        isRotated = value
    }

    companion object {
        const val KEY_CODE = "code"
        const val KEY_ROTATED = "rotated"
    }
}