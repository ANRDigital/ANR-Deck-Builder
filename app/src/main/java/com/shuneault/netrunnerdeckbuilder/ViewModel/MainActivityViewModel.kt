package com.shuneault.netrunnerdeckbuilder.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.game.Deck
import com.shuneault.netrunnerdeckbuilder.helper.Sorter.DeckSorter

class MainActivityViewModel(private val cardRepo: CardRepository, private val deckRepo: IDeckRepository) : ViewModel() {
    private var _side: MutableLiveData<String> = MutableLiveData(Card.Side.SIDE_RUNNER)
    var side: String
        get() = _side.value!!
        set(value) {
            _side.value = value
        }

    fun getLiveDecksForSide(side: String): LiveData<List<Deck>> {
        return Transformations.map(deckRepo.allDecksLiveData()) {
            it.sortWith(DeckSorter())
            it.filter { deck -> deck.isSide(side) }
        }
    }

    // Called when the choose identity activity returns when creating a new deck (ListDeckFragment)
    fun createDeck(identityCardCode: String?): Deck {
        val identity = cardRepo.getCard(identityCardCode)
        val format = cardRepo.defaultFormat
        val deck = Deck(identity, format)
        deckRepo.createDeck(deck)
        return deck
    }

    fun starDeck(deck: Deck, isStarred: Boolean) {
        deck.isStarred = isStarred
        deckRepo.saveDeck(deck)
    }

    fun cloneDeck(deck: Deck) {
        deckRepo.cloneDeck(deck)
    }

}