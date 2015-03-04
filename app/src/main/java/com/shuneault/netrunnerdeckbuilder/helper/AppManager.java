package com.shuneault.netrunnerdeckbuilder.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.prefs.ListPreferenceMultiSelect;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class AppManager {

    private final static AppManager mAppManager = new AppManager();

    /* File management */
    public static final String EXT_CARDS_IMAGES = ".png";
    public static final String FILE_CARDS_JSON = "cards.json";
    public static final String FILE_DECKS_JSON = "decks.json";
    public static final String FILE_DECKS_JSON_TEMP = "decks.json.tmp";
    public static final String FILE_DECKS_JSON_BACKUP = "decks.json.bkp";

    // Logcat
    public static final String LOGCAT = "com.example.netrunnerdeckbuilder.LOGCAT";

    // Database stuff
    public static final String DATABASE_NAME = "data";
    public static final int DATABASE_VERSION = 1;

    // Notifications
    public static final int NOTIFY_DOWNLOAD_CARDS_ID = 1;
    public static final int NOTIFY_DOWNLOAD_IMAGES_ID = 2;

    // App Context
    private Context mContext;

    // Shared Preferences
    private SharedPreferences mSharedPrefs;

    // Database
    private DatabaseHelper mDb;

    private ArrayList<Deck> mDecks;
    private CardList mCards;

    private AppManager() {
        mDecks = new ArrayList<Deck>();
        mCards = new CardList();
    }

    public static AppManager getInstance() {
        if (mAppManager == null)
            new AppManager();
        return mAppManager;
    }

    public void init(Context context) {
        // Initialize the application, database, shared preferences, etc.
        AppManager app = getInstance();
        mContext = context;
        mDb = new DatabaseHelper(context);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(context).build());
    }

    public SharedPreferences getSharedPrefs() {
        return mSharedPrefs;
    }

    public DatabaseHelper getDatabase() {
        return mDb;
    }

    public ArrayList<Deck> getAllDecks() {
        return mDecks;
    }

    public CardList getAllCards() {
        return mCards;
    }

    public CardList getCardsFromDataPacksToDisplay() {
        // Return all cards if set in the preferences
        if (mSharedPrefs.getBoolean(SettingsActivity.KEY_PREF_DISPLAY_ALL_DATA_PACKS, true)) {
            return getAllCards();
        }

        // Return only the data packs requested
        CardList cd = new CardList();
        ArrayList<String> arrDataPacks = new ArrayList<String>(Arrays.asList(ListPreferenceMultiSelect.parseStoredValue(mSharedPrefs.getString(SettingsActivity.KEY_PREF_DATA_PACKS_TO_DISPLAY, ""))));
        for (Card card : this.mCards) {
            if (arrDataPacks.contains(card.getSetName())) {
                cd.add(card);
            }
        }
        return cd;
    }

    public ArrayList<String> getSetNames() {
        ArrayList<String> arr = new ArrayList<String>();
        for (Card card : this.mCards) {
            if (!arr.contains(card.getSetName())) {
                arr.add(card.getSetName());
            }
        }
        return arr;
    }

    public CardList getCardsBySetName(String setName) {
        CardList arrList = new CardList();
        for (Card card : this.mCards) {
            if (card.getSetName().equals(setName) && !arrList.contains(card)) {
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

    public int getNumberImagesCached(Context context) {
        int numImages = 0;
        CardList cards = AppManager.getInstance().getAllCards();
        for (Card card : cards) {
            if (card.isImageFileExists(context))
                numImages++;
        }
        return numImages;
    }

    public JSONArray getJSONCardsFile(Context context) throws IOException, JSONException {
        // Load the file in memory and return a JSON array
        InputStream in = context.openFileInput(AppManager.FILE_CARDS_JSON);
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

    public JSONArray getJSONDecksFile(Context context) throws IOException, JSONException {
        // Load the file in memory and return a JSON array
        InputStream in = context.openFileInput(AppManager.FILE_DECKS_JSON);
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
//	
//	public  void saveDecksToFile(Context context) {
//		// Generate a JSON array
//		JSONArray arrJSON = new JSONArray();
//		for (Deck deck : mDecks) {
//			JSONObject json = deck.toJSON();
//			arrJSON.put(json);
//		}
//		
//		// Write to a temporary file
//		try {
//			FileOutputStream out = context.openFileOutput(FILE_DECKS_JSON_TEMP, Context.MODE_PRIVATE);
//			out.write(arrJSON.toString().getBytes());
//			out.close();
//			// No error, overwrite the other file
//			File fileTemp = context.getFileStreamPath(FILE_DECKS_JSON_TEMP);
//			File fileOld = context.getFileStreamPath(FILE_DECKS_JSON);
//			fileOld.renameTo(context.getFileStreamPath(FILE_DECKS_JSON_BACKUP));
//			fileTemp.renameTo(context.getFileStreamPath(FILE_DECKS_JSON));
//			
//		} catch (FileNotFoundException e) {
//			// 
//			e.printStackTrace();
//		} catch (IOException e) {
//			// 
//			e.printStackTrace();
//		}
//		
//	}

}
