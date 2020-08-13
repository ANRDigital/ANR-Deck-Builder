package com.shuneault.netrunnerdeckbuilder.db

import androidx.lifecycle.MutableLiveData
import com.shuneault.netrunnerdeckbuilder.game.Deck
import com.shuneault.netrunnerdeckbuilder.game.Format
import java.util.ArrayList

interface IDeckRepository {
    fun getDeck(deckId: Long): Deck?
    fun allDecksLiveData(): MutableLiveData<ArrayList<Deck>>
    val allDecks: ArrayList<Deck>
    fun addDeck(deck: Deck)
    fun deleteDeck(deck: Deck): Boolean
    fun changeIdentity(mDeck: Deck, identityCode: String)
    fun cloneDeck(deck: Deck): Long
    fun createDeck(deck: Deck)
    fun setDeckFormat(mDeck: Deck, format: Format)
    fun saveDeck(deck: Deck)
    fun updateDeck(deck: Deck)
}