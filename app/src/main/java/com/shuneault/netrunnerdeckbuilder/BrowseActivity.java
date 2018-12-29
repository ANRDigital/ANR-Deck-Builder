package com.shuneault.netrunnerdeckbuilder;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import kotlin.Lazy;

import android.view.Menu;
import android.view.MenuItem;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.fragments.BrowseCardsFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.ChoosePacksDialogFragment;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider;

import java.util.ArrayList;

import static com.shuneault.netrunnerdeckbuilder.game.Format.FORMAT_ETERNAL;
import static org.koin.java.standalone.KoinJavaComponent.inject;

public class BrowseActivity extends AppCompatActivity implements BrowseCardsFragment.OnBrowseCardsClickListener,ChoosePacksDialogFragment.ChoosePacksDialogListener {

    private ArrayList<String> mPackFilter = new ArrayList<>();
    private Lazy<CardRepository> repo = inject(CardRepository.class);
    private Lazy<ISettingsProvider> settingsProvider = inject(ISettingsProvider.class);

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
                choosePacksDlg.setData(mPackFilter, repo.getValue().getFormat(FORMAT_ETERNAL));
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

    @Override
    public void onMyCollectionChosen(DialogFragment dialog) {
        mPackFilter = settingsProvider.getValue().getMyCollection();

        // update list
        BrowseCardsFragment frag = (BrowseCardsFragment) getSupportFragmentManager().findFragmentById(R.id.browse_fragment);
        frag.updatePackFilter(mPackFilter);
    }
}
