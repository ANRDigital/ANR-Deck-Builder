package com.shuneault.netrunnerdeckbuilder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.shuneault.netrunnerdeckbuilder.fragments.BrowseCardsFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.ChoosePacksDialogFragment;
import com.shuneault.netrunnerdeckbuilder.game.Card;

import java.util.ArrayList;

public class BrowseActivity extends AppCompatActivity implements BrowseCardsFragment.OnBrowseCardsClickListener,ChoosePacksDialogFragment.ChoosePacksDialogListener {

    private ArrayList<String> mPackFilter = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.browse_cards);

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.title_browse_cards);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) MenuItemCompat.getActionView(searchItem);

        sv.setOnQueryTextListener(
                new SearchView.OnQueryTextListener(){

                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        //todo: update search results
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        BrowseCardsFragment frag = (BrowseCardsFragment) getSupportFragmentManager().findFragmentById(R.id.browse_fragment);
                        frag.onQueryTextChange(s);
                        return false;
                    }
                }
        );

        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ChoosePacksDialogFragment choosePacksDlg = new ChoosePacksDialogFragment();
                choosePacksDlg.setPackFilter(mPackFilter);
                choosePacksDlg.show(getSupportFragmentManager(), "choosePacks");
                return false;
            }
        });
        return true;
    }

    @Override
    public void onCardClicked(Card card) {
        // do nothing for now
        Intent intent = new Intent(this, ViewDeckFullscreenActivity.class);
        intent.putExtra(ViewDeckFullscreenActivity.EXTRA_CARD_CODE, card.getCode());
        startActivity(intent);
    }

    @Override
    public void onChoosePacksDialogPositiveClick(DialogFragment dialog) {
        // save the new setting
        ChoosePacksDialogFragment dlg = (ChoosePacksDialogFragment)dialog;
        mPackFilter = dlg.getSelectedValues();

        // update list
        BrowseCardsFragment frag = (BrowseCardsFragment) getSupportFragmentManager().findFragmentById(R.id.browse_fragment);
        frag.updatePackFilter(mPackFilter);
    }
}
