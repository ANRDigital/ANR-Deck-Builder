package com.shuneault.netrunnerdeckbuilder.externalactivities;

import static org.koin.java.KoinJavaComponent.inject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shuneault.netrunnerdeckbuilder.DeckActivity;
import com.shuneault.netrunnerdeckbuilder.MainActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.importer.UniversalImporter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import kotlin.Lazy;


public class ImportDecksActivity extends AppCompatActivity {

    private Lazy<IDeckRepository> deckRepo = inject(IDeckRepository.class);
    Lazy<CardRepository> cardRepo = inject(CardRepository.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Intent
        Intent intent = getIntent();

        // Get the file as string
        Uri fileUri = intent.getData();
        String fileName = getDeckNameFromOctgnFile(fileUri);

        String fileData = openFile(fileUri);

        // Depending on the file, import one or multiple decks
        try {
            if (fileData != null) {
                // Get all decks
                ArrayList<Deck> decks = new UniversalImporter(fileData, cardRepo.getValue(), fileName).toDecks();
                for (Deck deck : decks) {
                    deckRepo.getValue().createDeck(deck);
                }

                // Inform how many decks were imported
                String deckList = "";
                for (Deck deck : decks) {
                    deckList = deckList + "\n- " + deck.getName();
                }
                Toast.makeText(this, this.getResources().getQuantityString(R.plurals.deck_imported_successfully, decks.size(), decks.size(), deckList), Toast.LENGTH_SHORT).show();

                // Open the newly imported deck, if only one was imported
                if (decks.size() == 1) {
                    Intent newActivity = new Intent(this, DeckActivity.class);
                    newActivity.putExtra(DeckActivity.ARGUMENT_DECK_ID, decks.get(0).getRowId());
                    startActivity(newActivity);
                    finish();
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            }
        } catch (Exception e) {
            alertMustUpdateCardList();
        }

    }

    @NotNull
    private String getDeckNameFromOctgnFile(Uri fileUri) {
        // assuming the file is octgn from netrunnerdb
        String filename = fileUri.getLastPathSegment();
        String temp = filename.substring(0, filename.lastIndexOf('.')); // remove extension
        temp = temp.replace('-', ' '); // replace hyphens
        return temp;
    }

    private void alertMustUpdateCardList() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.msg_error_importing_deck_title));
        builder.setMessage(getString(R.string.msg_error_importing_deck));
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
