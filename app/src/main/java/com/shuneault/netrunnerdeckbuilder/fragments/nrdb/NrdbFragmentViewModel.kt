package com.shuneault.netrunnerdeckbuilder.fragments.nrdb

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shuneault.netrunnerdeckbuilder.api.NrdbClient
import com.shuneault.netrunnerdeckbuilder.api.NrdbDeckLists
import com.shuneault.netrunnerdeckbuilder.api.NrdbHelper
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.game.Deck
import com.shuneault.netrunnerdeckbuilder.helper.NrdbDeckFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NrdbFragmentViewModel(private val cardRepository: CardRepository) : ViewModel() {
    private var todaysDeckLists = MutableLiveData<ArrayList<Deck>>()

    fun toggleNrdbSignIn(context: Context) {
        NrdbHelper.doNrdbSignIn(context)
    }

    fun getPublicDeckLists(context: Context): MutableLiveData<ArrayList<Deck>> {
        // trigger async refresh
        val today = Date()
        getDateDeckLists(today, context, ::updateDateDeckLists)

        return todaysDeckLists
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

    private fun getDateDeckLists(today: Date, context: Context,
                                 onSuccess: (Response<NrdbDeckLists>)->Unit) {
        val dateString = SimpleDateFormat("yyyy-MM-dd").format(today)

        val apiService = NrdbClient().getApiService(context)
        val call = apiService.getDateDeckLists(dateString)
        call.enqueue(object : Callback<NrdbDeckLists>{
            override fun onResponse(call: Call<NrdbDeckLists>, response: Response<NrdbDeckLists>) {
                onSuccess(response)
            }

            override fun onFailure(call: Call<NrdbDeckLists>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }


}
