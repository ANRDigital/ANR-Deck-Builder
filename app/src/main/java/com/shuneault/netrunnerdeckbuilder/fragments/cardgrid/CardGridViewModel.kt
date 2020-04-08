package com.shuneault.netrunnerdeckbuilder.fragments.cardgrid

import androidx.lifecycle.ViewModel
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository
import com.shuneault.netrunnerdeckbuilder.game.CardCount
import com.shuneault.netrunnerdeckbuilder.helper.Sorter
import java.util.*

class CardGridViewModel(val deckRepository: IDeckRepository) : ViewModel() {
    var deckId: Long = 0
    val cardCounts: ArrayList<CardCount> = ArrayList()
    var setName: String = ""
    var title: String = ""

    fun loadDeck(deckId: Long) {
        val deck = deckRepository.getDeck(deckId)
        cardCounts.addAll(deck.cardCounts)
        cardCounts.sortWith(Sorter.CardCountSorterByTypeThenName());
        title = deck.name
        this.deckId = deckId
    }
}
