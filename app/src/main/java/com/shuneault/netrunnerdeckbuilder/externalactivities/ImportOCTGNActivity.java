package com.shuneault.netrunnerdeckbuilder.externalactivities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.MainActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.octgn.OCTGN;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class ImportOCTGNActivity extends Activity {

    private DatabaseHelper mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the intent
        Intent intent = getIntent();

        // Initialize the database
        mDb = AppManager.getInstance().getDatabase();

        // Load the cards and decks
        if (AppManager.getInstance().getAllCards().size() <= 0) {
            doLoadCards();
        }

        // Ask for a new deck name
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(true);
        pDialog.setMessage(getResources().getText(R.string.please_wait));

        pDialog.setCancelable(true);
        pDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        pDialog.show();

        // Download the image
        if (intent.getData().getScheme().equals("file")) {
            // File opened through file explorer
            Deck deck = OCTGN.toDeck(getImportData(getIntent()));
            addImportedDeck(deck);
        } else if (intent.getData().getScheme().equals("http")) {
            // Opened through the browser, download the file
            StringDownloader sd = new StringDownloader(new StringDownloaderListener() {

                @Override
                public void onDownloadFinish(String result) {
                    Deck deck = OCTGN.toDeck(result);
                    addImportedDeck(deck);
                }
            });
            sd.execute(intent.getDataString());
        } else if (intent.getData().getScheme().equals("content")) {
            // Opened through Gmail or another mail application
            Deck deck = OCTGN.toDeck(getImportData(getIntent()));
            addImportedDeck(deck);
        }

    }

    private void alertMustUpdateCardList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.msg_error_importing_deck));
        builder.setMessage(getString(R.string.msg_error_update_card_list));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    private void addImportedDeck(Deck deck) {
        if (deck == null) {
            alertMustUpdateCardList();
            return;
        }
        // Import the deck with a new name
        deck.setName("[IMP] " + deck.getName());
        // Add the deck
        AppManager.getInstance().addDeck(deck);
        (new DatabaseHelper(this)).createDeck(deck);
        // Toast
        Toast.makeText(this, R.string.deck_imported_successfully, Toast.LENGTH_SHORT).show();
        // Launch the new deck
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_DECK_ID, deck.getRowId());
        startActivity(intent);
        finish();
    }

    /**
     * @param intent
     * @return String representation of the XML file
     */
    private String getImportData(Intent intent) {
        try {
            InputStream in = getContentResolver().openInputStream(intent.getData());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                sb.append(line);

            String strResult = sb.toString();
            in.close();

            return strResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private interface StringDownloaderListener {
        void onDownloadFinish(String result);
    }

    private class StringDownloader extends AsyncTask<String, Void, String> {

        private StringDownloaderListener mListener;

        public StringDownloader(StringDownloaderListener listener) {
            this.mListener = listener;
        }

        @Override
        protected String doInBackground(String... params) {

            // Download the file
            try {
                URL url = new URL(params[0]);
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url.toURI());
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null)
                    sb.append(line);

                String strResult = sb.toString();
                is.close();

                return strResult;
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Nothing
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mListener.onDownloadFinish(result);
        }

    }


    public void doLoadCards() {
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
            doLoadDecks();

        } catch (FileNotFoundException e) {
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doLoadDecks() {
        // Load the decks from the DB
        AppManager.getInstance().getAllDecks().clear();
        AppManager.getInstance().getAllDecks().addAll(mDb.getAllDecks(true));
    }


}
