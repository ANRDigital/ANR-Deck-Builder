package com.shuneault.netrunnerdeckbuilder;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.shuneault.netrunnerdeckbuilder.adapters.CardCountImageAdapter;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardCount;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;

import java.util.ArrayList;
import java.util.Collections;


public class ViewCardsAsGridActivity extends AppCompatActivity {
    // Arguments
    public static final String EXTRA_DECK_ID = "com.example.netrunnerdeckbuilder.EXTRA_DECK_ID";
    public static final String EXTRA_SET_NAME = "com.example.netrunnerdeckbuilder.EXTRA_SET_NAME";
    public static final String EXTRA_CARD_CODE = "com.example.netrunnerdeckbuilder.EXTRA_CARD_CODE";

    private String mSetName = null;
    private Long mDeckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deck_grid);

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        // GUI
        GridView mGridView = (GridView) findViewById(R.id.gridView);

        // Get the deck / set name
        mDeckId = getIntent().getLongExtra(EXTRA_DECK_ID, 0);
        AppManager appManager = AppManager.getInstance();
        Deck mDeck = appManager.getDeck(mDeckId);
        mSetName = getIntent().getStringExtra(EXTRA_SET_NAME);

        // Build the cards array
        ArrayList<CardCount> cardCounts = new ArrayList<>();

        ArrayList<Card> mCards;
        if (mDeck != null) {
            getSupportActionBar().setIcon(mDeck.getIdentity().getFactionImageRes(this));
            setTitle(mDeck.getName());
            cardCounts = mDeck.getCardCounts();
            Collections.sort(cardCounts, new Sorter.CardCountSorterByTypeThenName());
        } else {
            mCards = appManager.getCardRepository().getPackCards(mSetName);
            Collections.sort(mCards, new Sorter.CardSorterByCardNumber());
            for (Pack pack : appManager.getAllPacks()) {
                if (pack.getCode().equals(mSetName)) {
                    setTitle(pack.getName());
                    break;
                }
            }
            for (Card c : mCards) {
                cardCounts.add(new CardCount(c, 0));
            }
        }

        // Quit if deck is empty
        if (cardCounts.isEmpty()) {
            alertEmptyDeck();
        } else {
            mGridView.setAdapter(new CardCountImageAdapter(this, cardCounts));
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ViewCardsAsGridActivity.this, ViewDeckFullscreenActivity.class);
                    intent.putExtra(ViewDeckFullscreenActivity.EXTRA_DECK_ID, mDeckId);
                    intent.putExtra(ViewDeckFullscreenActivity.EXTRA_SET_NAME, mSetName);
                    intent.putExtra(ViewDeckFullscreenActivity.EXTRA_POSITION, position);
                    startActivity(intent);
                }
            });
        }
    }

    private void alertEmptyDeck() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.view_deck);
        builder.setMessage(R.string.deck_is_empty);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> finish());
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return false;
    }

}
