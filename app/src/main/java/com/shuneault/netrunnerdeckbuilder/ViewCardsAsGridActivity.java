package com.shuneault.netrunnerdeckbuilder;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.shuneault.netrunnerdeckbuilder.adapters.CardCountImageAdapter;
import com.shuneault.netrunnerdeckbuilder.fragments.cardgrid.CardGridViewModel;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardCount;
import com.shuneault.netrunnerdeckbuilder.helper.NrdbHelper;

import static org.koin.java.standalone.KoinJavaComponent.get;

public class ViewCardsAsGridActivity extends AppCompatActivity {
    // Arguments
    public static final String EXTRA_DECK_ID = "com.example.netrunnerdeckbuilder.EXTRA_DECK_ID";

    private CardGridViewModel viewModel = get(CardGridViewModel.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        setContentView(R.layout.activity_view_deck_grid);
        // Get the deck
        long mDeckId = getIntent().getLongExtra(EXTRA_DECK_ID, 0);
        viewModel.loadDeck(mDeckId);
        setTitle(viewModel.getTitle());

        // Quit if deck is empty
        if (viewModel.getCardCounts().isEmpty()) {
            alertEmptyDeck();
        }

        // GUI
        GridView gridView = findViewById(R.id.gridView);
        gridView.setAdapter(new CardCountImageAdapter(this, viewModel.getCardCounts()));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ViewCardsAsGridActivity.this, ViewDeckFullscreenActivity.class);
                intent.putExtra(ViewDeckFullscreenActivity.EXTRA_DECK_ID, viewModel.getDeckId());
                intent.putExtra(ViewDeckFullscreenActivity.EXTRA_POSITION, position);
                startActivity(intent);
            }
        });
        gridView.setOnItemLongClickListener((adapterView, view, pos, id) -> {
            CardCount item = (CardCount) adapterView.getItemAtPosition(pos);
            Card card = item.getCard();
            NrdbHelper.ShowNrdbWebPage(this, card);
            return true;
        });

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
    public boolean onSupportNavigateUp() {
        finish();

        return false;
    }

}
