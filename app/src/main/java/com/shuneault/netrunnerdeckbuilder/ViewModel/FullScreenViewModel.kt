package com.shuneault.netrunnerdeckbuilder.ViewModel

import androidx.lifecycle.ViewModel
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.game.CardCount
import com.shuneault.netrunnerdeckbuilder.game.Deck
import com.shuneault.netrunnerdeckbuilder.helper.Sorter
import com.shuneault.netrunnerdeckbuilder.helper.Sorter.CardSorterByCardType
import java.util.*
import kotlin.collections.ArrayList

class FullScreenViewModel(private val deckRepository: IDeckRepository, private val cardRepository: CardRepository) : ViewModel() {
    val cardCounts: ArrayList<CardCount> = ArrayList()
    var size: Int = 0
        get() = this.cardCounts.size

    var setName: String? = null
    private var cardCode: String? = null
    var position = 0
    // NO Card Sort
    var cardCodes: ArrayList<String> = ArrayList()
        set(cardCodes) {
            field.addAll(cardCodes)
            cardCounts.clear()

            for (code: String in cardCodes){
                cardCounts.add(CardCount(cardRepository.getCard(code), 0))
            }
            // NO Card Sort
        }
    private var deck: Deck? = null

    fun setCardCode(cardCode: String?) {
        cardCode?.let {
            this.cardCode = it
            cardCounts.add(CardCount(cardRepository.getCard(it), 0))
        }
    }

    fun loadDeck(deckId: Long) {
        deck = deckRepository.getDeck(deckId)
        updateCounts()
    }

    fun setDeck(deck: Deck){
        this.deck = deck
        updateCounts()
    }

    private fun updateCounts() {
        deck?.let {
            cardCounts.clear()
            cardCounts.addAll(it.cardCounts)
            cardCounts.sortWith(Sorter.CardCountSorterByTypeThenName());
            cardCounts.add(0, CardCount(it.identity, 1))
        }
    }

    fun getCurrentCardTitle(): String {
        val cc: CardCount = cardCounts[position]
        deck?.let {
            val num = it.getCardCount(cc.card);
            return String.format("%d x %s", num, cc.card.title)
        }
        return cc.card.title
    }

    fun getCurrentCard(): Card {
        val cc: CardCount = cardCounts[position]
        return cc.card
    }

    fun isEmpty(): Boolean {
        return cardCounts.isEmpty()
    }

    fun getDeckTitle(): CharSequence? {
        return deck?.name
    }

    val factionCode: String?
        get() {
            if (deck != null) {
                return deck!!.factionCode
            }
            return if (cardCode != null) {
                cardCounts[0].card.factionCode
            } else null
        }

}