package com.shuneault.netrunnerdeckbuilder.game
import java.util.ArrayList

class CardCountList : ArrayList<CardCount?>() {
    fun getCardCount(card: Card): CardCount? {
        for (i in this.indices) {
            if (this[i]!!.card.code == card.code) {
                return this[i]
            }
        }
        return null
    }

    fun setCount(card: Card, count: Int) {
        val cardCount = getCardCount(card)
        if (cardCount != null) {
            cardCount.count = count
        } else {
            this.add(CardCount(card, count))
        }
    }
}