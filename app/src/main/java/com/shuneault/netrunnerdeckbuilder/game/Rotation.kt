package com.shuneault.netrunnerdeckbuilder.game

import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class Rotation {
    private var name = ""
    val cycles = ArrayList<String>()
    var code = ""

    constructor(rotationJSON: JSONObject) {
        try {
            this.code = rotationJSON.getString("code")
            name = rotationJSON.getString("name")
            val packArray = rotationJSON.getJSONArray("cycles")
            for (i in 0 until packArray.length()) {
                val cycleCode = packArray.getString(i)
                cycles.add(cycleCode)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    constructor() {}
}