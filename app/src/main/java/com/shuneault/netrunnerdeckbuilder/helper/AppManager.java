package com.shuneault.netrunnerdeckbuilder.helper;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.prefs.ListPreferenceMultiSelect;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sebast on 24/01/16.
 */
public class AppManager extends Application {

    /* File management */
    public static final String EXT_CARDS_IMAGES = ".png";
    public static final String FILE_CARDS_JSON = "cards.json";
    public static final String FILE_DECKS_JSON = "decks.json";
    public static final String FILE_DECKS_JSON_TEMP = "decks.json.tmp";
    public static final String FILE_DECKS_JSON_BACKUP = "decks.json.bkp";

    // Logcat
    public static final String LOGCAT = "com.example.netrunnerdeckbuilder.LOGCAT";

    private static AppManager mInstance;
    private DatabaseHelper mDb;

    // Decks
    private ArrayList<Deck> mDecks = new ArrayList<>();
    private CardList mCards = new CardList();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mDb = new DatabaseHelper(this);
        doLoadCards();
        mDecks.addAll(mDb.getAllDecks(true));
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

    public int getNumberImagesCached() {
        int numImages = 0;
        CardList cards = getAllCards();
        for (Card card : cards) {
            if (card.isImageFileExists(this))
                numImages++;
        }
        return numImages;
    }

    public JSONArray getJSONCardsFile() throws IOException, JSONException {
        // Load the file in memory and return a JSON array
        InputStream in = openFileInput(FILE_CARDS_JSON);
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
            JSONArray jsonFile = AppManager.getInstance().getJSONCardsFile();
            CardList arrCards = AppManager.getInstance().getAllCards();
            arrCards.clear();
            for (int i = 0; i < jsonFile.length(); i++) {
                // Create the card and add to the array
                //		Do not load cards from the Alternates set
                Card card = new Card(jsonFile.getJSONObject(i));
                if (!card.getSetName().equals(Card.SetName.ALTERNATES))
                    arrCards.add(card);
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
        CardDownloader dl = new CardDownloader(this, new CardDownloader.CardDownloaderListener() {

            ProgressDialog mDialog;

            @Override
            public void onTaskCompleted() {
                // Load the cards in the app
                doLoadCards();

//                // Close the dialog
//                if (mDialog != null)
//                    mDialog.dismiss();

                // Ask if we want to download the images on if almost no images are downloaded
//                if (AppManager.getInstance().getNumberImagesCached() < 20) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                    builder.setMessage(R.string.download_all_images_question_first_launch);
//                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // Download all images
//                            CardImagesDownloader dnl = new CardImagesDownloader(AppManager.this, new CardImagesDownloader.CardImagesDownloaderListener() {
//
//                                @Override
//                                public void onTaskCompleted() {
//
//                                }
//
//                                @Override
//                                public void onImageDownloaded(Card card, int count, int max) {
//
//                                }
//
//                                @Override
//                                public void onBeforeStartTask(Context context, int max) {
//
//                                }
//                            });
//                            dnl.execute();
//                        }
//                    });
//                    builder.setNegativeButton(android.R.string.no, null);
//                    builder.create().show();
//                }

            }

            @Override
            public void onBeforeStartTask(Context context) {
//                // Display a progress dialog
//                mDialog = new ProgressDialog(context);
//                mDialog.setTitle(getResources().getString(R.string.downloading_cards));
//                mDialog.setIndeterminate(true);
//                mDialog.setCancelable(false);
//                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//                mDialog.setMessage(null);
//                mDialog.show();
//                Toast.makeText(AppManager.this, R.string.downloading_cards, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadError() {
                // Display the error and cancel the ongoing dialog
//                mDialog.dismiss();
//                Toast.makeText(AppManager.this, R.string.cards_download_complete, Toast.LENGTH_SHORT).show();
//
//                // If zero cards are available, exit the application
//                if (AppManager.getInstance().getAllCards().size() <= 0) {
//                    Toast.makeText(AppManager.this, R.string.error_downloading_cards_quit, Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(AppManager.this, R.string.error_downloading_cards, Toast.LENGTH_LONG).show();
//                }
            }
        });
        dl.execute();
    }

}
