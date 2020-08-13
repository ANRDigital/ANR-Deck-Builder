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
import java.util.*
import kotlin.collections.ArrayList

class MainActivityViewModel(private val cardRepo: CardRepository, private val deckRepo: IDeckRepository) : ViewModel() {
    private var _side: MutableLiveData<String> = MutableLiveData(Card.Side.SIDE_RUNNER)
    var side: String
        get() = _side.value!!
        set(value) {
            _side.value = value
        }

    private val mCurrentDecks = MutableLiveData<ArrayList<Deck>>() // this needs renaming or splitting into runner / corp??

    // Only the selected tab decks
    fun getDecksForSide(side: String): MutableLiveData<ArrayList<Deck>> {
        val decks = ArrayList<Deck>()

        // Only the selected tab decks
        for (deck in deckRepo.allDecks) {
            if (deck != null && deck.side == side) {
                decks.add(deck)
            }
        }
        // Sort the list
        Collections.sort(decks, DeckSorter())
        mCurrentDecks.value = decks
        return mCurrentDecks
    }

    fun getLiveDecksForSide(side: String): LiveData<List<Deck>> {
        return Transformations.map(deckRepo.allDecksLiveData()) {
            it.sortWith(DeckSorter())
            it.filter { deck -> deck.isSide(side) }
        }
    }

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
        //todo: sort decks
//        Collections.sort(mCurrentDecks, new Sorter.DeckSorter());
    }

    fun cloneDeck(deck: Deck) {
        deckRepo.cloneDeck(deck)
    }

}