package com.shuneault.netrunnerdeckbuilder;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.shuneault.netrunnerdeckbuilder.ViewModel.DeckActivityViewModel;
import com.shuneault.netrunnerdeckbuilder.export.JintekiNet;
import com.shuneault.netrunnerdeckbuilder.export.OCTGN;
import com.shuneault.netrunnerdeckbuilder.export.PlainText;
import com.shuneault.netrunnerdeckbuilder.fragments.ChoosePacksDialogFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckBuildFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckCardsFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckHandFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckInfoFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckMyCardsFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckStatsFragment;
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider;
import com.shuneault.netrunnerdeckbuilder.interfaces.IDeckViewModelProvider;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;
import com.shuneault.netrunnerdeckbuilder.ui.ThemeHelper;
import com.shuneault.netrunnerdeckbuilder.util.SlidingTabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import kotlin.Lazy;

import static org.koin.java.standalone.KoinJavaComponent.get;
import static org.koin.java.standalone.KoinJavaComponent.inject;

public class DeckActivity extends AppCompatActivity implements OnDeckChangedListener,
        ChoosePacksDialogFragment.ChoosePacksDialogListener, IDeckViewModelProvider {

    // Activity Result
    public static final int REQUEST_CHANGE_IDENTITY = 2;

    public static final String ARGUMENT_DECK_ID = "com.shuneault.netrunnerdeckbuilder.ARGUMENT_DECK_ID";
    public static final String ARGUMENT_SELECTED_TAB = "com.shuneault.netrunnerdeckbuilder.ARGUMENT_SELECTED_TAB";

    private LinearLayout layoutFiltered;
    private TextView lblInfoInfluence;
    private TextView lblInfoCards;
    private TextView lblInfoAgenda;
    private TextView lblInfoLegal;

    private int mSelectedTab = 0;

    private DeckActivityViewModel viewModel = get(DeckActivityViewModel.class);

    private Lazy<ISettingsProvider> settingsProvider = inject(ISettingsProvider.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the theme and layout
        long deckId = getIntent().getExtras().getLong(ARGUMENT_DECK_ID);
        viewModel.setDeckId(deckId);

        try {
            setTheme(ThemeHelper.Companion.getTheme(viewModel.getDeckFactionCode(), this));
        } catch (Exception e) {
            // do nothing, will use default blue theme instead
            e.printStackTrace();
        }

        // super must be called after setTheme or else notification and navigation bars won't be themed properly
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_deck);

        // GUI
        ViewPager mViewPager = findViewById(R.id.pager);
        LinearLayout layoutAgendas = findViewById(R.id.layoutAgendas);
        layoutFiltered = findViewById(R.id.layoutFiltered);
        lblInfoInfluence = findViewById(R.id.lblInfoInfluence);
        lblInfoCards = findViewById(R.id.lblInfoCards);
        lblInfoAgenda = findViewById(R.id.lblInfoAgenda);
        lblInfoLegal = findViewById(R.id.lblInfoLegal);


        // ActionBar - set elevation to 0 to remove shadow
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setElevation(0);
        }

        // Get the params
        if (savedInstanceState != null) {
            mSelectedTab = savedInstanceState.getInt(ARGUMENT_SELECTED_TAB);
        } else {
            mSelectedTab = getIntent().getExtras().getInt(ARGUMENT_SELECTED_TAB);
        }

        // Change the title
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(viewModel.getDeck().getName());
        // app icon doesn't work with support library - needs implemented differently
//        if (mDeck.getIdentity().getFactionCode().equals(Card.Faction.FACTION_NEUTRAL)) {
//            mActionBar.setLogo(getResources().getDrawable(R.drawable.ic_launcher));
//        } else {
//            mActionBar.setLogo(mDeck.getIdentity().getFactionImageRes(this));
//        }

        // Display the agendas (in the infobar) only if it is a CORP deck
        if (viewModel.getDeck().getSide().equals(Card.Side.SIDE_CORPORATION)) {
            layoutAgendas.setVisibility(View.VISIBLE);
        } else {
            layoutAgendas.setVisibility(View.GONE);
        }

        setPackFilterIconVisibility();

        layoutFiltered.setOnClickListener(v -> doChoosePacks());

        // Update the infobar
        updateInfoBar();

        // Set the page adapter
        mViewPager.setAdapter(new DeckTabsPagerAdapter(getSupportFragmentManager()));

        // attach tabs to view pager
        SlidingTabLayout tabs = findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);
        if (viewModel.getDeck().getFactionCode().startsWith(Card.Faction.FACTION_NEUTRAL)) {
            tabs.setBackgroundColor(getResources().getColor(R.color.netrunner_blue));
        } else {
            tabs.setBackgroundColor(getResources().getColor(getResources().getIdentifier(
                    "dark_" + viewModel.getDeck().getFactionCode().replace("-", ""), "color", this.
                            getPackageName())));
        }
    }

    private void setPackFilterIconVisibility() {
        Format format = viewModel.getDeck().getFormat();
        if (format.canFilter()) {
            layoutFiltered.setVisibility(View.VISIBLE);
        } else {
            layoutFiltered.setVisibility(View.GONE);
        }
    }

    private void updateInfoBar() {
        // Update the influence, card count and agendas
        Deck deck = viewModel.getDeck();
        if (deck.getInfluenceLimit() == Integer.MAX_VALUE) {
            lblInfoInfluence.setText(deck.getDeckInfluence() + "/" + getResources().getString(R.string.infinite_symbol));
        } else {
            lblInfoInfluence.setText(deck.getDeckInfluence() + "/" + deck.getInfluenceLimit());
        }
        lblInfoCards.setText(deck.getDeckSize() + "/" + deck.getMinimumDeckSize());
        lblInfoAgenda.setText(deck.getDeckAgenda() + "/(" + deck.getDeckAgendaMinimum() + '-' + (deck.getDeckAgendaMinimum() + 1) + ')');

        // Update the style Influence
        if (deck.isInfluenceOk())
            lblInfoInfluence.setTextAppearance(this, R.style.InfoBarGood);
        else
            lblInfoInfluence.setTextAppearance(this, R.style.InfoBarBad);
        // Update the style Agendas
        if (deck.isAgendaOk())
            lblInfoAgenda.setTextAppearance(this, R.style.InfoBarGood);
        else
            lblInfoAgenda.setTextAppearance(this, R.style.InfoBarBad);
        // Update the style Cards
        if (deck.isCardCountOk())
            lblInfoCards.setTextAppearance(this, R.style.InfoBarGood);
        else
            lblInfoCards.setTextAppearance(this, R.style.InfoBarBad);

        if (viewModel.isValid()) {
            lblInfoLegal.setTextAppearance(this, R.style.InfoBarGood);
            lblInfoLegal.setText("✓");
        } else {
            lblInfoLegal.setTextAppearance(this, R.style.InfoBarBad);
            lblInfoLegal.setText("✗");
        }

        setPackFilterIconVisibility();
    }

    @Override
    public DeckActivityViewModel getViewModel() {
        return this.viewModel;
    }

    public class DeckTabsPagerAdapter extends FragmentPagerAdapter {

        public DeckTabsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int arg0) {
            switch (arg0) {
                case 1:
                    return new DeckMyCardsFragment();
                case 2:
                    return new DeckCardsFragment();
                case 3:
                    return new DeckBuildFragment();
                case 4:
                    return new DeckStatsFragment();
                case 5:
                    return new DeckHandFragment();
                default:
                    return new DeckInfoFragment();
            }
        }

        @Override
        public int getCount() {
            //
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.tab_info);
                case 1:
                    return getResources().getString(R.string.tab_my_cards);
                case 2:
                    return getResources().getString(R.string.tab_cards);
                case 3:
                    return getResources().getString(R.string.tab_build);
                case 4:
                    return getResources().getString(R.string.tab_stats);
                case 5:
                    return getResources().getString(R.string.tab_hand);
            }
            return "";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.deck, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mnuDeleteDeck:
                // Alert
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle(R.string.delete_deck);
                builder.setMessage(R.string.message_delete_deck);
                builder.setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewModel.deleteDeck(viewModel.getDeck());
                        Toast.makeText(DeckActivity.this, R.string.message_deck_deleted, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();

                return true;
            case R.id.mnuCloneDeck:
                // Create a clone of the deck
                Long newDeckId = viewModel.cloneDeck(viewModel.getDeck());
                Toast.makeText(this, R.string.toast_deck_cloned_successfuly, Toast.LENGTH_LONG).show();
                // Start the new deck activity
                Intent intentClone = new Intent(DeckActivity.this, DeckActivity.class);
                intentClone.putExtra(DeckActivity.ARGUMENT_DECK_ID, newDeckId);
                intentClone.putExtra(DeckActivity.ARGUMENT_SELECTED_TAB, 0);
                startActivity(intentClone);
                // Close this activity
                finish();
                return true;

            case R.id.mnuViewFullScreen:
                Intent intentFullScreen = new Intent(this, DeckViewActivity.class);
                intentFullScreen.putExtra(DeckActivity.ARGUMENT_DECK_ID, viewModel.getDeck().getRowId());
                startActivity(intentFullScreen);
                return true;

            case R.id.mnuChangeIdentity:
                // Change the identity
                Intent intentChooseIdentity = new Intent(this, ChooseIdentityActivity.class);
                intentChooseIdentity.putExtra(ChooseIdentityActivity.EXTRA_SIDE_CODE, viewModel.getDeck().getSide());
                intentChooseIdentity.putExtra(ChooseIdentityActivity.EXTRA_FORMAT, viewModel.getDeck().getFormat().getId());
                intentChooseIdentity.putExtra(ChooseIdentityActivity.EXTRA_INITIAL_IDENTITY_CODE, viewModel.getDeck().getIdentity().getCode());
                startActivityForResult(intentChooseIdentity, REQUEST_CHANGE_IDENTITY);
                return true;

            case R.id.mnuOCTGN:

                String filename = viewModel.getDeck().getFileSafeName() + ".o8d";
                // Save the file as OCTGN format
                try {
                    FileOutputStream fileOut = this.openFileOutput(filename, Context.MODE_PRIVATE);
                    fileOut.write(new OCTGN().fromDeck(viewModel.getDeck()).getBytes());
                    fileOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Create the send intent
                Intent intentEmail = new Intent(Intent.ACTION_SEND);
                intentEmail.setType("text/plain");
                intentEmail.putExtra(Intent.EXTRA_SUBJECT, "NetRunner Deck - " + viewModel.getDeck().getName());
                intentEmail.putExtra(Intent.EXTRA_TEXT, "\r\n\r\nDownload Android Netrunner DeckBuilder for free at https://play.google.com/store/apps/details?id=com.shuneault.netrunnerdeckbuilder");

                File fileStreamPath = getFileStreamPath(filename);
                Uri fileUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID,
                        fileStreamPath);

                intentEmail.putExtra(Intent.EXTRA_STREAM, fileUri);
                startActivity(Intent.createChooser(intentEmail, getText(R.string.menu_share)));

                return true;

            case R.id.mnuPlainText:
                String plainText = new PlainText(this).fromDeck(viewModel.getDeck());

                // Create the send intent
                Intent intentEmailPlain = new Intent(Intent.ACTION_SEND);
                intentEmailPlain.setType("text/plain");
                intentEmailPlain.putExtra(Intent.EXTRA_SUBJECT, "NetRunner Deck - " + viewModel.getDeck().getName());
                intentEmailPlain.putExtra(Intent.EXTRA_TEXT, plainText + "\n\nDownload Android Netrunner DeckBuilder for free at https://play.google.com/store/apps/details?id=com.shuneault.netrunnerdeckbuilder");
                startActivity(Intent.createChooser(intentEmailPlain, getText(R.string.menu_share)));

                return true;

            case R.id.mnuJintekiNet:
                String jintekiNet = new JintekiNet().fromDeck(viewModel.getDeck());

                // Create the send intent
                Intent intentJintekiNetPlain = new Intent(Intent.ACTION_SEND);
                intentJintekiNetPlain.setType("text/plain");
                intentJintekiNetPlain.putExtra(Intent.EXTRA_SUBJECT, "NetRunner Deck - " + viewModel.getDeck().getName());
                intentJintekiNetPlain.putExtra(Intent.EXTRA_TEXT, jintekiNet);
                startActivity(Intent.createChooser(intentJintekiNetPlain, getText(R.string.menu_share)));

                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.mnuSetPacks:
                doChoosePacks();
                return true;

            case R.id.mnuCoreCount:
                doSetCoreCount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void doChoosePacks() {
        // display list alert dialog
        Deck deck = viewModel.getDeck();
        ChoosePacksDialogFragment dialog = new ChoosePacksDialogFragment(deck.getPackFilter(), deck.getFormat(), false);
        dialog.show(getSupportFragmentManager(), "choosePacks");
    }

    private void doSetCoreCount() {
        AtomicInteger choice = new AtomicInteger();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.set_core_count)
            .setSingleChoiceItems(R.array.arrCoreCountPreference, viewModel.getDeck().getCoreCount(),
                    (dialog, which) -> choice.set(which))
            .setPositiveButton(R.string.ok, (dialog, which) -> {
                int count = choice.get();
                viewModel.setCoreCount(count); // which is zero based array
                doCardPoolChange();

                dialog.dismiss();
            })
            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                dialog.dismiss();
            });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onChoosePacksDialogPositiveClick(DialogFragment dialog) {
        // save the new setting
        ChoosePacksDialogFragment frag = (ChoosePacksDialogFragment)dialog;
        ArrayList<String> packFilter = frag.getSelectedValues();
        viewModel.setPackFilter(packFilter);

        doCardPoolChange();
        updateInfoBar();
    }

    @Override
    public void onMyCollectionChosen(DialogFragment dialog) {
        ArrayList<String> myCollection = settingsProvider.getValue().getMyCollection();
        viewModel.setPackFilter(myCollection);

        doCardPoolChange();
        updateInfoBar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_CHANGE_IDENTITY:
                String idCode = data.getStringExtra(ChooseIdentityActivity.EXTRA_IDENTITY_CODE);
                viewModel.changeDeckIdentity(viewModel.getDeck(), idCode);

                // Restart the activity
                Intent intent = new Intent(DeckActivity.this, DeckActivity.class);
                intent.putExtra(DeckActivity.ARGUMENT_DECK_ID, viewModel.getDeck().getRowId());
                intent.putExtra(DeckActivity.ARGUMENT_SELECTED_TAB, mSelectedTab);
                startActivity(intent);
                finish();

                break;
        }
    }

    @Override
    public void onDeckCardsChanged() {
        // Update the infobar
        updateInfoBar();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f: fragments) {
            if (f instanceof DeckMyCardsFragment)
                ((DeckMyCardsFragment) f).onDeckCardsChanged();
            if(f instanceof DeckCardsFragment)
                ((DeckCardsFragment) f).onDeckCardsChanged();
        }
    }

    @Override
    public void onFormatChanged(Format format) {
        DeckActivityViewModel vm = viewModel;
        if (vm.changeDeckFormat(format)) {
            doCardPoolChange();
        }

    }

    private void doCardPoolChange() {
        updateInfoBar();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f: fragments) {
            if (f instanceof DeckMyCardsFragment)
                ((DeckMyCardsFragment) f).onFormatChanged();
            if(f instanceof DeckCardsFragment)
                ((DeckCardsFragment) f).onFormatChanged();
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        // Save the deck
        Handler myHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = () -> viewModel.save();
        myHandler.post(myRunnable);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(ARGUMENT_DECK_ID, viewModel.getDeck().getRowId());
        outState.putInt(ARGUMENT_SELECTED_TAB, mSelectedTab);

        super.onSaveInstanceState(outState);
    }

}
