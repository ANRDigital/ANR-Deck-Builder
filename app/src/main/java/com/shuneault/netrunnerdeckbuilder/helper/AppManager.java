package com.shuneault.netrunnerdeckbuilder.helper;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.shuneault.netrunnerdeckbuilder.MyApplication;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository;
import com.shuneault.netrunnerdeckbuilder.db.JSONDataLoader;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Pack;

import java.util.ArrayList;
import java.util.Calendar;

import kotlin.Lazy;

import static com.shuneault.netrunnerdeckbuilder.fragments.SettingsFragment.SHARED_PREF_LAST_UPDATE_DATE;
import static org.koin.java.standalone.KoinJavaComponent.inject;

/**
 * Created by sebast on 24/01/16.
 */
public class AppManager extends MyApplication {

    /* File management */
    public static final String EXT_CARDS_IMAGES = ".png";

    // Shared Prefd

    // Logcat
    public static final String LOGCAT = "LOGCAT";

    private static AppManager mInstance;
    private DatabaseHelper mDb;

    // Decks
    private ArrayList<Deck> mDecks = new ArrayList<>();

    public CardRepository mCardRepo;
    public CardRepository getCardRepository() {
        return mCardRepo;
    }

    private Lazy<IDeckRepository> mDeckRepo = inject(IDeckRepository.class);

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mDb = new DatabaseHelper(this);

        ISettingsProvider settingsProvider = new SettingsProvider(this);
        JSONDataLoader fileLoader = new JSONDataLoader(new LocalFileHelper(this));
        mCardRepo = new CardRepository(this, settingsProvider, fileLoader);

        mDecks.addAll(mDeckRepo.getValue().getAllDecks());

        // Download the card list every week
        try {
            Calendar today = Calendar.getInstance();
            Calendar lastUpdate = Calendar.getInstance();
            lastUpdate.setTimeInMillis(getSharedPrefs().getLong(SHARED_PREF_LAST_UPDATE_DATE, 0));
            if (today.getTimeInMillis() - lastUpdate.getTimeInMillis() > (24 * 60 * 60 * 1000 * 7)) {
                Log.i(LOGCAT, "Weekly download...");

                mCardRepo.doDownloadAllData();
                // update last download date
                getSharedPrefs().edit()
                        .putLong(SHARED_PREF_LAST_UPDATE_DATE, Calendar.getInstance().getTimeInMillis())
                        .apply();
            }
        } catch (Exception ignored) {
            //todo: flag a message here?`
        }
    }

    public static AppManager getInstance() {
        return mInstance;
    }

    public SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    public DatabaseHelper getDatabase() {
        return mDb;
    }

    public ArrayList<Deck> getAllDecks() {
        return mDeckRepo.getValue().getAllDecks();
    }

    public ArrayList<Pack> getAllPacks() {
        return mCardRepo.getPacks(true);
    }

    public ArrayList<String> getSetNames() {
        return mCardRepo.getPackNames();
    }

    // Return the requested card
    public Card getCard(String code) {
        return getCardRepository().getCard(code);
    }

    // decks with rowId of 128 and higher wouldn't load so
    // pass in a primitive long instead of Long object due to this
    // explanation here: http://bexhuff.com/java-autoboxing-wackiness
    public Deck getDeck(long rowId) {
        return mDeckRepo.getValue().getDeck(rowId);
    }

}
