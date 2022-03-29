package com.shuneault.netrunnerdeckbuilder.api

import java.util.*

data class NrdbDeckLists (
    val data: List<NrdbDeckList>
)

// Represents the data as returned from NRDB api calls
data class NrdbDeckList(
    val id: Int,
    val name: String,
    val description: String,
    val cards: Map<String, Int>,
    val date_creation: Date,
    val date_update: Date,
    val mwl_code: String,
    val tags: String = ""
){
    fun getCardCounts(): ArrayList<ItemCount>{
        val result = ArrayList<ItemCount>()
        for (key in cards.keys){
            result.add(ItemCount(key, cards.getValue(key)))
        }
        return result
    }
}

data class ItemCount(val code: String, val count: Int) {
}
