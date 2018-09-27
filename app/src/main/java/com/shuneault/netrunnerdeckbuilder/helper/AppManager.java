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
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.game.NetRunnerBD;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.prefs.ListPreferenceMultiSelect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mDb = new DatabaseHelper(this);
        doLoadCards();
        doLoadPacks();
        mDecks.addAll(mDb.getAllDecks(true));

        // Download the card list every week
        try {
            Calendar today = Calendar.getInstance();
            Calendar lastUpdate = Calendar.getInstance();
            lastUpdate.setTimeInMillis(getSharedPrefs().getLong(SHARED_PREF_LAST_UPDATE_DATE, 0));
            if (today.getTimeInMillis() - lastUpdate.getTimeInMillis() > (24 * 60 * 60 * 1000 * 7)) {
                Log.i(LOGCAT, "Weekly download...");
                doDownloadCards();
            }
        } catch (Exception ignored) {
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
        CardList cd = new CardList();
        ArrayList<String> arrDataPacks = new ArrayList<String>(Arrays.asList(ListPreferenceMultiSelect.parseStoredValue(getSharedPrefs().getString(SettingsActivity.KEY_PREF_DATA_PACKS_TO_DISPLAY, ""))));
        for (Card card : this.mCards) {
            if (arrDataPacks.contains(card.getSetName())) {
                cd.add(card);
            }
        }
        return cd;
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
        for (Card theCard : mCards) {
            if (theCard.getCode().equals(code))
                return theCard;
        }
        return null;
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

    public int getNumberImagesCached() {
        int numImages = 0;
        CardList cards = getAllCards();
        for (Card card : cards) {
            if (card.isImageFileExists(this))
                numImages++;
        }
        return numImages;
    }

    private JSONObject getJSONCardsFile() throws IOException, JSONException {
        // Load the file in memory and return a JSON array
        InputStream in;
        try {
            in = openFileInput(FILE_CARDS_JSON);
        } catch (FileNotFoundException e) {
            in = getResources().openRawResource(R.raw.cardsv2);
        }
        InputStreamReader fs = new InputStreamReader(in);
        BufferedReader bfs = new BufferedReader(fs);
        String theLine = null;
        StringBuilder theStringBuilder = new StringBuilder();
        // Read the file
        while ((theLine = bfs.readLine()) != null)
            theStringBuilder.append(theLine);

        JSONObject jsonFile = new JSONObject(theStringBuilder.toString());
        bfs.close();
        fs.close();
        in.close();
        return jsonFile;
    }

    private JSONObject getJSONPacksFile() throws IOException, JSONException {
        // Load the file in memory and return a JSON array
        InputStream in;
        try {
            in = openFileInput(FILE_PACKS_JSON);
        } catch (FileNotFoundException e) {
            in = getResources().openRawResource(R.raw.packs);
        }
        InputStreamReader fs = new InputStreamReader(in);
        BufferedReader bfs = new BufferedReader(fs);
        String theLine = null;
        StringBuilder theStringBuilder = new StringBuilder();
        // Read the file
        while ((theLine = bfs.readLine()) != null)
            theStringBuilder.append(theLine);

        JSONObject jsonFile = new JSONObject(theStringBuilder.toString());
        bfs.close();
        fs.close();
        in.close();
        return jsonFile;
    }

    private JSONObject getJSON_MWLFile() throws IOException, JSONException {
        // Load the file in memory and return a JSON array
        InputStream in;
        try {
            in = openFileInput(FILE_MWL_JSON);
        } catch (FileNotFoundException e) {
            in = getResources().openRawResource(R.raw.mwl);
        }
        InputStreamReader fs = new InputStreamReader(in);
        BufferedReader bfs = new BufferedReader(fs);
        String theLine = null;
        StringBuilder theStringBuilder = new StringBuilder();
        // Read the file
        while ((theLine = bfs.readLine()) != null)
            theStringBuilder.append(theLine);

        JSONObject jsonFile = new JSONObject(theStringBuilder.toString());
        bfs.close();
        fs.close();
        in.close();
        return jsonFile;
    }

    public JSONArray getJSONDecksFile() throws IOException, JSONException {
        // Load the file in memory and return a JSON array
        InputStream in = openFileInput(FILE_DECKS_JSON);
        InputStreamReader fs = new InputStreamReader(in);
        BufferedReader bfs = new BufferedReader(fs);
        String theLine = null;
        StringBuilder theStringBuilder = new StringBuilder();
        // Read the file
        while ((theLine = bfs.readLine()) != null)
            theStringBuilder.append(theLine);

        JSONArray jsonFile = new JSONArray(theStringBuilder.toString());
        bfs.close();
        fs.close();
        in.close();
        return jsonFile;
    }

    private void doLoadPacks() {
        // Packs downloaded, load them
        try {
            JSONObject jsonFile = AppManager.getInstance().getJSONPacksFile();
            JSONArray jsonPacks = jsonFile.getJSONArray("data");
            ArrayList<Pack> arrPacks = AppManager.getInstance().getAllPacks();
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
            // Most Wanted List
            // Get mwl data file as for use later
            JSONObject mJsonMWLfile = AppManager.getInstance().getJSON_MWLFile();
            JSONArray mMWLData = mJsonMWLfile.getJSONArray("data");
            for (int i = 0; i < mMWLData.length(); i++) {
                JSONObject mwlJSON = mMWLData.getJSONObject(i);
                if (mwlJSON.has("active")){
                    mActiveMWL = new MostWantedList(mwlJSON);
                }
            }

            HashMap<String, JSONObject> mMWLInfluences = new HashMap<>();
            JSONObject jsonMWLCards = mMWLData
                    .getJSONObject(2)
                    .getJSONObject("cards");
            Iterator<String> iterCards = jsonMWLCards.keys();
            while (iterCards.hasNext()) {
                String cardCode = iterCards.next();
                mMWLInfluences.put(cardCode, jsonMWLCards.getJSONObject(cardCode));
            }

            // The cards
            JSONObject jsonFile = AppManager.getInstance().getJSONCardsFile();
            JSONArray jsonCards = jsonFile.getJSONArray("data");
            CardList arrCards = AppManager.getInstance().getAllCards();
            arrCards.clear();
            for (int i = 0; i < jsonCards.length(); i++) {
                // Create the card and add to the array
                //		Do not load cards from the Alternates set
                JSONObject jsonCard = jsonCards.getJSONObject(i);
                JSONObject jsonLocale = jsonCard.optJSONObject("_locale");
                if (jsonLocale != null) {
                    JSONObject jsonLocaleProps = jsonLocale.getJSONObject(AppManager.getInstance().getSharedPrefs().getString(SettingsActivity.KEY_PREF_LANGUAGE, "en"));
                    Iterator<String> iter = jsonLocaleProps.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        jsonCard.put(key, jsonLocaleProps.getString(key));
                    }
                }
                String cardCode = jsonCard.getString(Card.NAME_CODE);
                jsonCard.put(Card.NAME_IMAGE_SRC, jsonFile.getString("imageUrlTemplate").replace("{code}", cardCode));

                // New Card
                int cardUniversalCost = 0;
                if (mMWLInfluences.containsKey(cardCode)) {
                    cardUniversalCost = mMWLInfluences.get(cardCode).optInt("universal_faction_cost", 0);
                }
                arrCards.add(new Card(jsonCard, cardUniversalCost));
            }

            // Load the decks
//            doLoadDecks();

        } catch (FileNotFoundException e) {

            doDownloadCards();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doDownloadCards() {
        // Most Wanted List
        StringDownloader sdMWL = new StringDownloader(this, NetRunnerBD.getMWLUrl(), AppManager.FILE_MWL_JSON, new StringDownloader.FileDownloaderListener() {

            @Override
            public void onBeforeTask() {

            }

            @Override
            public void onTaskComplete(String s) {

                // Cards List
                StringDownloader sdCards = new StringDownloader(AppManager.this, NetRunnerBD.getAllCardsUrl(), AppManager.FILE_CARDS_JSON, new StringDownloader.FileDownloaderListener() {
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
            }

            @Override
            public void onError(Exception e) {

            }
        });
        sdMWL.execute();

        StringDownloader sdPacks = new StringDownloader(this, NetRunnerBD.getAllPacksUrl(), AppManager.FILE_PACKS_JSON, new StringDownloader.FileDownloaderListener() {
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

    public DeckValidator getDeckValidator(){
        // create validator
        return new DeckValidator(mActiveMWL);
    }
}
