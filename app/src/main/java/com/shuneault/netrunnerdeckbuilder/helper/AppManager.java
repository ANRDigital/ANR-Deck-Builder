package com.shuneault.netrunnerdeckbuilder.helper;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardBuilder;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.game.NetRunnerBD;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.prefs.ListPreferenceMultiSelect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by sebast on 24/01/16.
 */
public class AppManager extends Application {

    /* File management */
    public static final String EXT_CARDS_IMAGES = ".png";
    public static final String FILE_CARDS_JSON = "cardsv2.json";
    public static final String FILE_DECKS_JSON = "decks.json";
    public static final String FILE_PACKS_JSON = "packs.json";
    public static final String FILE_MWL_JSON = "mwl.json";

    // Shared Prefd
    public static final String SHARED_PREF_LAST_UPDATE_DATE = "SHARED_PREF_LAST_UPDATE_DATE";

    // Logcat
    public static final String LOGCAT = "LOGCAT";

    private static AppManager mInstance;
    private DatabaseHelper mDb;

    // Decks
    private ArrayList<Deck> mDecks = new ArrayList<>();
    private CardList mCards = new CardList();
    private ArrayList<Pack> mPacks = new ArrayList<>();
    private MostWantedList mActiveMWL;
    private HashMap<String, JSONObject> mMWLInfluences = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mDb = new DatabaseHelper(this);
        doLoadMWL();
        doLoadCards();
        doLoadPacks();
        mDecks.addAll(mDb.getAllDecks(true, mCards));

        // Download the card list every week
        try {
            Calendar today = Calendar.getInstance();
            Calendar lastUpdate = Calendar.getInstance();
            lastUpdate.setTimeInMillis(getSharedPrefs().getLong(SHARED_PREF_LAST_UPDATE_DATE, 0));
            if (today.getTimeInMillis() - lastUpdate.getTimeInMillis() > (24 * 60 * 60 * 1000 * 7)) {
                Log.i(LOGCAT, "Weekly download...");
                doDownloadCards();
                doDownloadMWL();
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
        return mDecks;
    }

    public ArrayList<Pack> getAllPacks() {
        return mPacks;
    }

    public Pack getPackByCode(String code) {
        for (Pack pack : mPacks) {
            if (pack.getCode().equals(code)) {
                return pack;
            }
        }
        return null;
    }

    public CardList getAllCards() {
        return mCards;
    }

    public CardList getCardsFromDataPacksToDisplay() {
        // Return all cards if set in the preferences
        if (getSharedPrefs().getBoolean(SettingsActivity.KEY_PREF_DISPLAY_ALL_DATA_PACKS, true)) {
            return getAllCards();
        }

        // Return only the data packs requested
        String packsPref = getSharedPrefs().getString(SettingsActivity.KEY_PREF_DATA_PACKS_TO_DISPLAY, "");
        ArrayList<String> packs = new ArrayList<>(Arrays.asList(ListPreferenceMultiSelect.parseStoredValue(packsPref)));
        return mCards.getPacks(packs);
    }


    public CardList getCardsFromDataPacksToDisplay(ArrayList<String> packs) {
        if (packs.size() > 0)
        {
            return mCards.getPacks(packs);
        }
        else
            return getCardsFromDataPacksToDisplay();
    }

    public ArrayList<String> getSetNames() {
        ArrayList<String> arr = new ArrayList<String>();
        for (Pack pack : mPacks) {
            arr.add(pack.getName());
        }
        return arr;
    }

    public CardList getCardsBySetName(String setName) {
        CardList arrList = new CardList();
        for (Card card : this.mCards) {
            if (card.getSetCode().equals(setName) && !arrList.contains(card)) {
                arrList.add(card);
            }
        }
        return arrList;
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
        return mCards.getCard(code);
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

    private void doLoadPacks() {
        // Packs downloaded, load them
        try {
            JSONObject jsonFile = LocalFileHelper.getJSONPacksFile(this, FILE_PACKS_JSON);
            JSONArray jsonPacks = jsonFile.getJSONArray("data");
            ArrayList<Pack> arrPacks = mPacks;
            arrPacks.clear();
            for (int i = 0; i < jsonPacks.length(); i++) {
                Pack pack = new Pack(jsonPacks.getJSONObject(i));
                arrPacks.add(pack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doLoadCards() {
        // Cards downloaded, load them
        try {
            /* Load the card list
             *
             * - Create the card
             * - Add the card to the array
             * - Generate the faction list
             * - Generate the side list
             * - Generate the card set list
             *
             */

            JSONObject jsonFile = LocalFileHelper.getJSONCardsFile(this, FILE_CARDS_JSON);
            String imageUrlTemplate = jsonFile.getString("imageUrlTemplate");

            JSONArray jsonCards = jsonFile.getJSONArray("data");

            mCards.clear();
            for (int i = 0; i < jsonCards.length(); i++) {
                // Create the card and add to the array
                //		Do not load cards from the Alternates set
                JSONObject jsonCard = jsonCards.getJSONObject(i);
                JSONObject jsonLocale = jsonCard.optJSONObject("_locale");
                if (jsonLocale != null) {
                    String lanquagePref = this.getSharedPrefs().getString(SettingsActivity.KEY_PREF_LANGUAGE, "en");
                    JSONObject jsonLocaleProps = jsonLocale.getJSONObject(lanquagePref);
                    Iterator<String> iter = jsonLocaleProps.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        jsonCard.put(key, jsonLocaleProps.getString(key));
                    }
                }


                CardBuilder cardBuilder = new CardBuilder(imageUrlTemplate);
                Card card = cardBuilder.BuildFromJson(jsonCard);

                // New Card
                int cardUniversalCost = 0;
                if (mMWLInfluences.containsKey(card.getCode())) {
                    cardUniversalCost = mMWLInfluences.get(card.getCode()).optInt("universal_faction_cost", 0);
                }
                card.setMostWantedInfluence(cardUniversalCost);
                mCards.add(card);
            }

        } catch (FileNotFoundException e) {

            doDownloadCards();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doLoadMWL() {
        try {
            // Most Wanted List
            JSONObject mJsonMWLfile = LocalFileHelper.getJSON_MWLFile(this, FILE_MWL_JSON);
            JSONArray mMWLData = mJsonMWLfile.getJSONArray("data");
            for (int i = 0; i < mMWLData.length(); i++) {
                JSONObject mwlJSON = mMWLData.getJSONObject(i);
                if (mwlJSON.has("active")) {
                    mActiveMWL = new MostWantedList(mwlJSON);
                }
            }

            mMWLInfluences.clear();
            JSONObject jsonMWLCards = mMWLData
                    .getJSONObject(2)
                    .getJSONObject("cards");
            Iterator<String> iterCards = jsonMWLCards.keys();
            while (iterCards.hasNext()) {
                String cardCode = iterCards.next();
                mMWLInfluences.put(cardCode, jsonMWLCards.getJSONObject(cardCode));
            }

        } catch (FileNotFoundException e) {
            doDownloadMWL();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doDownloadMWL(){
        // Most Wanted List
        StringDownloader sdMWL = new StringDownloader(this, NetRunnerBD.getMWLUrl(), FILE_MWL_JSON, new StringDownloader.FileDownloaderListener() {

            @Override
            public void onBeforeTask() {

            }

            @Override
            public void onTaskComplete(String s) {
                doLoadMWL();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        sdMWL.execute();

    }

    public void doDownloadCards() {
        // Cards List
        StringDownloader sdCards = new StringDownloader(this, NetRunnerBD.getAllCardsUrl(), FILE_CARDS_JSON, new StringDownloader.FileDownloaderListener() {
            @Override
            public void onBeforeTask() {

            }

            @Override
            public void onTaskComplete(String s) {
                getSharedPrefs()
                        .edit()
                        .putLong(SHARED_PREF_LAST_UPDATE_DATE, Calendar.getInstance().getTimeInMillis())
                        .apply();
                doLoadCards();
                Toast.makeText(getApplicationContext(), R.string.card_list_updated_successfully, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        sdCards.execute();

        StringDownloader sdPacks = new StringDownloader(this, NetRunnerBD.getAllPacksUrl(), FILE_PACKS_JSON, new StringDownloader.FileDownloaderListener() {
            @Override
            public void onBeforeTask() {

            }

            @Override
            public void onTaskComplete(String s) {
                doLoadPacks();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        sdPacks.execute();

    }

    public DeckValidator getDeckValidator() {
        // create validator
        return new DeckValidator(mActiveMWL);
    }

    // return the default (active) mwl
    public MostWantedList getMWL() {
        return mActiveMWL;
    }

}
