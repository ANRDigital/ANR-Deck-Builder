package com.shuneault.netrunnerdeckbuilder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.shuneault.netrunnerdeckbuilder.adapters.CardsDeckImageAdapter;
import com.shuneault.netrunnerdeckbuilder.adapters.CardsImageAdapter;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;

import java.util.ArrayList;
import java.util.Collections;


public class ViewDeckGridActivity extends ActionBarActivity
{
    // Arguments
    public static final String EXTRA_DECK_ID = "com.example.netrunnerdeckbuilder.EXTRA_DECK_ID";
    public static final String EXTRA_SET_NAME = "com.example.netrunnerdeckbuilder.EXTRA_SET_NAME";
    public static final String EXTRA_CARD_CODE = "com.example.netrunnerdeckbuilder.EXTRA_CARD_CODE";

    // GUI
    private GridView mGridView;

    private Deck mDeck = null;
    private String mSetName = null;
    private Long mDeckId;
    private ArrayList<Card> mCards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deck_grid);

        // GUI
        mGridView = (GridView) findViewById(R.id.gridView);

        // Get the deck / set name
        mDeckId = getIntent().getLongExtra(EXTRA_DECK_ID, 0);
        mDeck = AppManager.getInstance().getDeck(mDeckId);
        mSetName = getIntent().getStringExtra(EXTRA_SET_NAME);

        // Build the cards array
        // todo: view all cards
        if (mSetName != null && mSetName.equals(getString(R.string.view_all_cards))) {
//            mCards = AppManager.getInstance().getAllCards();
//            Collections.sort(mCards, new Sorter.CardSorterByCardNumber());
//            setTitle(mSetName);
//            mGridView.setAdapter(new CardsImageAdapter(this, mCards));
//            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Intent intent = new Intent(ViewDeckGridActivity.this, ViewDeckFullscreenActivity.class);
//                    intent.putExtra(ViewDeckFullscreenActivity.EXTRA_SET_NAME, mSetName);
//                    intent.putExtra(ViewDeckFullscreenActivity.EXTRA_POSITION, position);
//                    startActivity(intent);
//                }
//            });
        } else if (mDeck != null) {
            mCards = mDeck.getCards();
            getSupportActionBar().setIcon(mDeck.getIdentity().getFactionImageRes(this));
            Collections.sort(mCards, new Sorter.CardSorterByCardType());
            setTitle(mDeck.getName());
            mGridView.setAdapter(new CardsDeckImageAdapter(this, mDeck, mCards));
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ViewDeckGridActivity.this, ViewDeckFullscreenActivity.class);
                    intent.putExtra(ViewDeckFullscreenActivity.EXTRA_DECK_ID, mDeckId);
                    intent.putExtra(ViewDeckFullscreenActivity.EXTRA_POSITION, position);
                    startActivity(intent);
                }
            });
        } else {
            mCards = AppManager.getInstance().getCardsBySetName(mSetName);
            Collections.sort(mCards, new Sorter.CardSorterByCardNumber());
            for (Pack pack : AppManager.getInstance().getAllPacks()) {
                if (pack.getCode().equals(mSetName)) {
                    setTitle(pack.getName());
                    break;
                }
            }
            mGridView.setAdapter(new CardsImageAdapter(this, mCards));
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ViewDeckGridActivity.this, ViewDeckFullscreenActivity.class);
                    intent.putExtra(ViewDeckFullscreenActivity.EXTRA_SET_NAME, mSetName);
                    intent.putExtra(ViewDeckFullscreenActivity.EXTRA_POSITION, position);
                    startActivity(intent);
                }
            });
        }

        // Quit if deck is empty
        if (mCards.size() == 0) {
            exitIfDeckEmpty();
            return;
        }


    }

    private void exitIfDeckEmpty() {
        if (mCards.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.view_deck);
            builder.setMessage(R.string.deck_is_empty);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        }
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
}
