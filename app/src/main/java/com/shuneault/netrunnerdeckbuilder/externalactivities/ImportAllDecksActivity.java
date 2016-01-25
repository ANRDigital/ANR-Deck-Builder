package com.shuneault.netrunnerdeckbuilder.externalactivities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.MainActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * Created by sebast on 11/01/16.
 */
public class ImportAllDecksActivity extends Activity {

    private DatabaseHelper mDb;
    private AppManager mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the intent
        Intent intent = getIntent();

        // Initialize the database + AppManager
        mApp = AppManager.getInstance();
        mDb = mApp.getDatabase();

        // Ask for a new deck name
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(true);
        pDialog.setMessage(getResources().getText(R.string.please_wait));

        pDialog.setCancelable(true);
        pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        pDialog.show();

        // Load the file
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(intent.getData())));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String strResult = sb.toString();
            reader.close();

            // Load the decks
            doLoadCards();

            // File read, load the data in decks
            JSONArray jsonArray = new JSONArray(strResult);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Deck deck = Deck.fromJSON(jsonObject);
                deck.setName("[IMP] " + deck.getName());
                mApp.addDeck(deck);
                mDb.createDeck(deck);
            }

            // Everything was successful, launch the main activity
            Toast.makeText(this, getString(R.string.decks_imported_successfully), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();


        } catch (Exception e) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.msg_error_importing_deck);
            alert.setMessage(R.string.msg_error_update_card_list);
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert.show();

            e.printStackTrace();
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
            JSONArray jsonFile = mApp.getJSONCardsFile();
            CardList arrCards = mApp.getAllCards();
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
        mApp.getAllDecks().clear();
        mApp.getAllDecks().addAll(mDb.getAllDecks(true));
    }
}