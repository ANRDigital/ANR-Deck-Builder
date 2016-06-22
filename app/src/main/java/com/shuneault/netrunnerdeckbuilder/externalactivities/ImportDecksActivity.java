package com.shuneault.netrunnerdeckbuilder.externalactivities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.MainActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.export.OCTGN;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ImportDecksActivity extends AppCompatActivity {

    private AppManager mApp;
    private DatabaseHelper mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Database and App Manager
        mApp = AppManager.getInstance();
        mDb = mApp.getDatabase();

        // Intent
        Intent intent = getIntent();

        // Get the file as string
        String theFile = openFile(intent.getData());

        // Depending on the file, import one or multiple decks
        try {
            if (theFile != null) {
                // Get all decks
                ArrayList<Deck> decks = OCTGN.getDecksFromString(theFile);
                for (Deck deck : decks) {
                    mApp.addDeck(deck);
                    mDb.createDeck(deck);
                }

                // Inform how many decks were imported
                Toast.makeText(this, R.string.decks_imported_successfully, Toast.LENGTH_SHORT).show();

                // Open the newly imported deck, if only one was imported
                Intent newActivity = new Intent(this, MainActivity.class);
                if (decks.size() == 1) {
                    newActivity.putExtra(MainActivity.EXTRA_DECK_ID, decks.get(0).getRowId());
                }
                startActivity(newActivity);
                finish();
            }
        } catch (Exception e) {
            alertMustUpdateCardList();
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

    private String openFile(Uri path) {
        try {
            InputStream in = getContentResolver().openInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
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
}
