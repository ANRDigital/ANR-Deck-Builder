package com.shuneault.netrunnerdeckbuilder.helper

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.util.Log
import com.shuneault.netrunnerdeckbuilder.MyApplication
import com.shuneault.netrunnerdeckbuilder.api.NrdbDeckLists
import com.shuneault.netrunnerdeckbuilder.api.NrdbHelper
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository
import com.shuneault.netrunnerdeckbuilder.db.JSONDataLoader
import com.shuneault.netrunnerdeckbuilder.fragments.SettingsFragment
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.game.Deck
import org.koin.java.KoinJavaComponent.inject
import retrofit2.Response
import java.util.*

/**
 * Created by sebast on 24/01/16.
 */
class AppManager : MyApplication() {
    var database: DatabaseHelper? = null
        private set
    var cardRepository: CardRepository? = null
    private val mDeckRepo = inject<IDeckRepository>(IDeckRepository::class.java)
    override fun onCreate() {
        super.onCreate()
        instance = this
        database = DatabaseHelper(this)
        val settingsProvider: ISettingsProvider = SettingsProvider(this)
        val fileLoader = JSONDataLoader(LocalFileHelper(this))
        cardRepository = CardRepository(this, settingsProvider, fileLoader)

        // Download the card list every week
        try {
            val today = Calendar.getInstance()
            val lastUpdate = Calendar.getInstance()
            lastUpdate.timeInMillis =
                sharedPrefs.getLong(SettingsFragment.SHARED_PREF_LAST_UPDATE_DATE, 0)
            if (today.timeInMillis - lastUpdate.timeInMillis > 24 * 60 * 60 * 1000 * 7) {
                Log.i(LOGCAT, "Weekly download...")
                cardRepository!!.doDownloadAllData()
                // update last download date
                sharedPrefs.edit()
                    .putLong(
                        SettingsFragment.SHARED_PREF_LAST_UPDATE_DATE,
                        Calendar.getInstance().timeInMillis
                    )
                    .apply()
            }
        } catch (ignored: Exception) {
            //todo: flag a message here?`
        }

        try {
            NrdbHelper.getMyPrivateDeckLists(applicationContext, ::updatePrivateDecks)
        } catch (ignored: Exception) {
            //todo: flag a message here?`
        }
    }

    private fun updatePrivateDecks(response: Response<NrdbDeckLists>) {
//        val result = ArrayList<Deck>()
//        val data = response.body()!!.data
//        for (nrdbDeckList in data) {
//            val deck = NrdbDeckFactory(cardRepository).convertToDeck(nrdbDeckList)
//            deck?.let { result.add(it) }
//        }
//
//        privateDecks.value = ArrayList<Deck>(result.sortedByDescending { d -> d.updated })
    }

    val sharedPrefs: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(this)
    val setNames: ArrayList<String>
        get() = cardRepository!!.packNames

    // Return the requested card
    fun getCard(code: String?): Card {
        return cardRepository!!.getCard(code)
    }

    // decks with rowId of 128 and higher wouldn't load so
    // pass in a primitive long instead of Long object due to this
    // explanation here: http://bexhuff.com/java-autoboxing-wackiness
    fun getDeck(rowId: Long): Deck? {
        return mDeckRepo.value.getDeck(rowId)
    }

    companion object {
        /* File management */
        const val EXT_CARDS_IMAGES = ".png"

        // Shared Prefd
        // Logcat
        const val LOGCAT = "LOGCAT"
        @JvmStatic
        var instance: AppManager? = null
            private set
    }
}