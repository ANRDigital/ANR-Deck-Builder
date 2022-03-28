package com.shuneault.netrunnerdeckbuilder.db

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.shuneault.netrunnerdeckbuilder.game.Deck
import com.shuneault.netrunnerdeckbuilder.game.Format
import com.shuneault.netrunnerdeckbuilder.util.DeckSort
import kotlin.collections.ArrayList

class DeckRepository(private val cardRepo: CardRepository, private val dbHelper: DatabaseHelper) : IDeckRepository {
    private val mLiveDeckData: MutableLiveData<ArrayList<Deck>> = MutableLiveData()

    private val mDecks: ArrayList<Deck> = dbHelper.getAllDecks(true, cardRepo)
    private val currentOrder = DeckSort.DATEASCENDING
    val decks = MediatorLiveData<List<Deck>>()

    init {
        mLiveDeckData.value = mDecks

        decks.addSource(mLiveDeckData){ result: List<Deck>? ->
            result?.let { decks.value = sortDecks(it, currentOrder)}
        }
    }

    private fun sortDecks(list: List<Deck>, currentOrder: Any): List<Deck>? {
        return list.sortedBy { it.name }
    }

    override fun getDeck(deckId: Long): Deck? {
        for (deck in mDecks) {
            if (deck.rowId == deckId) {
                return deck
            }
        }
        return null
    }

    override fun allDecksLiveData(): MutableLiveData<java.util.ArrayList<Deck>> {
        return mLiveDeckData
    }

    // this is used to export data, not display.
    override val allDecks: ArrayList<Deck>
        get() = mDecks

    override fun addDeck(deck: Deck) {
        mDecks.add(deck)
        refreshLiveData()
    }

    private fun refreshLiveData() {
        mLiveDeckData.value = mDecks
    }

    override fun deleteDeck(deck: Deck): Boolean {
        dbHelper.deleteDeck(deck)
        val result = mDecks.remove(deck)
        refreshLiveData()
        return result
    }

    override fun changeIdentity(deck: Deck, identityCode: String) {
        val identity = cardRepo.getCard(identityCode)
        deck.rowId?.let {
            val x = getDeck(it)
            x!!.identity = identity
            dbHelper.updateDeck(x)
        }
    }

    override fun cloneDeck(deck: Deck): Long {
        val newDeck = Deck.fromJSON(deck.toJSON(), cardRepo)
        newDeck.name = String.format("Copy of %1\$s", newDeck.name)
        // Do not clone the cards to add and cards to remove
        newDeck.cardsToAdd = ArrayList()
        newDeck.cardsToRemove = ArrayList()

        // Save in the database
        doCreateDeck(newDeck)
        return newDeck.rowId!!
    }

    private fun doCreateDeck(newDeck: Deck) {
        dbHelper.saveDeck(newDeck)
        addDeck(newDeck)
    }

    override fun createDeck(deck: Deck) {
        doCreateDeck(deck)
    }

    override fun setDeckFormat(deck: Deck, format: Format) {
        deck.format = format
        dbHelper.updateDeck(deck)
    }

    override fun saveDeck(deck: Deck) {
        dbHelper.saveDeck(deck)
        refreshLiveData()
    }

    override fun updateDeck(deck: Deck) {
        dbHelper.updateDeck(deck)
        refreshLiveData()
    }


}