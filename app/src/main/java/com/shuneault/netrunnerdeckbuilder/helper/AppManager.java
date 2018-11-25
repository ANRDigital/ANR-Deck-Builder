package com.shuneault.netrunnerdeckbuilder.helper;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.game.Pack;

import java.util.ArrayList;

/**
 * Created by sebast on 24/01/16.
 */
public class AppManager extends Application {

    /* File management */
    public static final String EXT_CARDS_IMAGES = ".png";

    // Shared Prefd
    public static final String SHARED_PREF_LAST_UPDATE_DATE = "SHARED_PREF_LAST_UPDATE_DATE";

    // Logcat
    public static final String LOGCAT = "LOGCAT";

    private static AppManager mInstance;
    private DatabaseHelper mDb;

    // Decks
    private ArrayList<Deck> mDecks = new ArrayList<>();

    private CardRepository mCardRepo;
    public CardRepository getCardRepository() {
        return mCardRepo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mDb = new DatabaseHelper(this);

        mCardRepo = new CardRepository(this);
        mDecks.addAll(mDb.getAllDecks(true, mCardRepo.getAllCards(), mCardRepo));

        // Download the card list every week
//        try {
//            Calendar today = Calendar.getInstance();
//            Calendar lastUpdate = Calendar.getInstance();
//            lastUpdate.setTimeInMillis(getSharedPrefs().getLong(SHARED_PREF_LAST_UPDATE_DATE, 0));
//            if (today.getTimeInMillis() - lastUpdate.getTimeInMillis() > (24 * 60 * 60 * 1000 * 7)) {
//                Log.i(LOGCAT, "Weekly download...");
//                doDownloadCards();
//                doDownloadMWL();
//            }
//        } catch (Exception ignored) {
//            //todo: flag a message here?`
//        }
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
        return mDecks;
    }

    public ArrayList<Pack> getAllPacks() {
        return mCardRepo.getPacks();
    }

    public Pack getPackByCode(String code) {
        return mCardRepo.getPack(code);
    }

    public CardList getAllCards() {
        return mCardRepo.getAllCards();
    }

    public CardList getCardsFromDataPacksToDisplay() {
        return mCardRepo.getCardsFromDataPacksToDisplay(new ArrayList<>());
    }


    public CardList getCardsFromDataPacksToDisplay(ArrayList<String> packFilter) {
        return mCardRepo.getCardsFromDataPacksToDisplay(packFilter);
    }

    public ArrayList<String> getSetNames() {
        return mCardRepo.getPackNames();
    }

    public CardList getCardsBySetName(String setName) {
        return mCardRepo.getPackCards(setName);
    }

    public void addDeck(Deck deck) {
        mDecks.add(deck);
    }

    public boolean deleteDeck(Deck deck) {
        mDb.deleteDeck(deck);
        return mDecks.remove(deck);
    }

    // Return the requested card
    public Card getCard(String code) {
        return mCardRepo.getCard(code);
    }

    // decks with rowId of 128 and higher wouldn't load so
    // pass in a primitive long instead of Long object due to this
    // explanation here: http://bexhuff.com/java-autoboxing-wackiness
    public Deck getDeck(long rowId) {
        for (Deck deck : this.mDecks) {
            if (deck.getRowId() == rowId) {
                return deck;
            }
        }
        return null;
    }



    public DeckValidator getDeckValidator() {
        // create validator
        return new DeckValidator(mCardRepo.getActiveMwl());
    }

    // return the default (active) mwl
    public MostWantedList getMWL() {
        return mCardRepo.getActiveMwl();
    }

}
