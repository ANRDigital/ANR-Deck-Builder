package com.shuneault.netrunnerdeckbuilder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.shuneault.netrunnerdeckbuilder.fragments.BrowseCardsFragment;
import com.shuneault.netrunnerdeckbuilder.game.Card;

public class BrowseActivity extends AppCompatActivity implements BrowseCardsFragment.OnBrowseCardsClickListener {

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

        return true;
    }

    @Override
    public void onCardClicked(Card card) {
        // do nothing for now
    }
}
