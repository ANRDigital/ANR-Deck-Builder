package com.shuneault.netrunnerdeckbuilder.fragments.nrdb

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.api.NrdbClient
import com.shuneault.netrunnerdeckbuilder.api.NrdbDeckLists
import com.shuneault.netrunnerdeckbuilder.api.NrdbHelper
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.db.DeckRepository
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository
import com.shuneault.netrunnerdeckbuilder.game.Deck
import com.shuneault.netrunnerdeckbuilder.helper.AppManager
import com.shuneault.netrunnerdeckbuilder.helper.NrdbDeckFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class NrdbFragmentViewModel(private val cardRepository: CardRepository, private val deckRepository: IDeckRepository) : ViewModel() {

    private var todaysDeckLists = MutableLiveData<ArrayList<Deck>>()
    private var privateDecks = MutableLiveData<ArrayList<Deck>>()

    fun toggleNrdbSignIn(context: Context) {
        NrdbHelper.doNrdbSignIn(context)
    }

    fun getNrdbDeckLists(context: Context): MutableLiveData<ArrayList<Deck>> {
        // trigger async refresh
        val today = Date()
        NrdbHelper.getDateDeckLists(today, context, ::updateDateDeckLists)

        return todaysDeckLists
    }

    fun getNrdbPrivateDecks(context: Context): MutableLiveData<ArrayList<Deck>> {
        NrdbHelper.getMyPrivateDeckLists(context, ::updatePrivateDecks)
        return privateDecks
    }

    private fun updateDateDeckLists(response: Response<NrdbDeckLists>) {
        val result = ArrayList<Deck>()
        val data = response.body()!!.data
        for (nrdbDeckList in data) {
            val deck = NrdbDeckFactory(cardRepository).convertToDeck(nrdbDeckList)
            result.add(deck)
        }
        todaysDeckLists.value = result
    }

    private fun updatePrivateDecks(response: Response<NrdbDeckLists>) {
        val result = ArrayList<Deck>()
        val data = response.body()!!.data
        for (nrdbDeckList in data) {
            val deck = NrdbDeckFactory(cardRepository).convertToDeck(nrdbDeckList)
            result.add(deck)
        }
        privateDecks.value = result
    }

    fun cloneDeck(deck: Deck) {
        deckRepository.cloneDeck(deck);
    }


}
