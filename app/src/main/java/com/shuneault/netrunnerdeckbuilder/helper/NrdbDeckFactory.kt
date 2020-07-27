package com.shuneault.netrunnerdeckbuilder.helper

import com.shuneault.netrunnerdeckbuilder.api.NrdbDeckList
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.game.CardCount
import com.shuneault.netrunnerdeckbuilder.game.Deck
import org.json.JSONObject
import java.util.*

class NrdbDeckFactory(val repo: CardRepository) {
    fun convertToDeck(deckList: NrdbDeckList): Deck {
        val format = repo.defaultFormat
        val cards = ArrayList<CardCount>()
        var identityCard: Card? = null

        for (item in deckList.getCardCounts()){
            val card = repo.getCard(item.code)
            if (card.isIdentity)
                identityCard = card
            else {
                val count = item.count
                cards.add(CardCount(card, count))
            }
        }
        val deck = Deck(identityCard, format)
        for (cc: CardCount in cards)
            deck.setCardCount(cc.card, cc.count)

        deck.nrdbId = deckList.id
        deck.name = deckList.name
        deck.notes = deckList.description
        return deck
    }

//    fun createDeckFromJson(json: JSONObject): Deck {
//        val format = repo.defaultFormat
//        val cards = ArrayList<CardCount>()
//        var identityCard: Card? = null
//
//        val jCards = json.getJSONObject("cards")
//        for (key: String in jCards.keys()){
//            val card = repo.getCard(key)
//            if (card.isIdentity)
//                identityCard = card
//            else {
//                val count: Int = jCards[key] as Int
//                cards.add(CardCount(card, count))
//            }
//        }
//        val deck = Deck(identityCard, format)
//        for (cc: CardCount in cards)
//            deck.setCardCount(cc.card, cc.count)
//
//        deck.nrdbId = json.getInt("id")
//        deck.name = json.getString("name")
//        deck.notes = json.getString("description")
//        return deck
//    }
}