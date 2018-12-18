package com.shuneault.netrunnerdeckbuilder.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.ViewDeckFullscreenActivity;
import com.shuneault.netrunnerdeckbuilder.adapters.ExpandableDeckCardListAdapter;
import com.shuneault.netrunnerdeckbuilder.adapters.ExpandableDeckCardListAdapter.OnButtonClickListener;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class DeckCardsFragment extends DeckActivityFragment implements MenuItemCompat.OnActionExpandListener {

    // Saved Instance
    private static final String SAVED_SELECTED_CARD_CODE = "CARD_CODE";

    private OnDeckChangedListener mListener;

    private Card currentCard;
    // TODO: Change to ExpandableStickyListAdapter https://github.com/emilsjolander/StickyListHeaders
    private ExpandableListView lstDeckCards;
    private ExpandableDeckCardListAdapter mDeckCardsAdapter;
    private HashMap<String, ArrayList<Card>> mListCards;
    private ArrayList<String> mHeaders;
    private SearchView sv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        // Inflate
        View mainView = inflater.inflate(R.layout.fragment_deck_cards, container, false);

        // The GUI items
        lstDeckCards = (ExpandableListView) mainView.findViewById(R.id.lstDeckCards);

        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set the list view
        setListView(super.mDeck);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do not inflate if already there
        if (menu.findItem(R.id.mnuSearch) == null)
            inflater.inflate(R.menu.deck_cards, menu);

        // Configure Search
        MenuItem item = menu.findItem(R.id.mnuSearch);
        sv = new SearchView(getActivity());
        item.setShowAsAction(MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        item.setActionView(sv);
        SearchView.SearchAutoComplete searchAutoComplete = sv.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(Color.WHITE);
        searchAutoComplete.setTextColor(Color.WHITE);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchAutoComplete, 0); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
        }
        sv.setIconifiedByDefault(false);
        ImageView searchIcon = (ImageView) sv.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

        // set underline
        View searchPlate = sv.findViewById(androidx.appcompat.R.id.search_plate);
        searchPlate.setBackgroundResource(R.drawable.search);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
                if (mDeckCardsAdapter != null) {
                    mDeckCardsAdapter.filterData(arg0);
                }
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(item, this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        try {
            mListener = (OnDeckChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDeckChangedListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Remember the selected card
        if (currentCard != null)
            outState.putString(SAVED_SELECTED_CARD_CODE, currentCard.getCode());
    }

    private void setListView(Deck deck) {
        // Get the headers
        mHeaders = activityViewModel.getCardHeaders();

        // Get the cards
        mListCards = activityViewModel.getGroupedCards(deck, mHeaders);

        // Set the adapter
        //todo: can we get this from the viewModel?
        CardRepository cardRepo = AppManager.getInstance().getCardRepository();
        mDeckCardsAdapter = new ExpandableDeckCardListAdapter(cardRepo, getActivity(), mHeaders, mListCards, deck, new OnButtonClickListener() {

            @Override
            public void onPlusClick(Card card) {
                activityViewModel.addCard(card);
                mListener.onDeckCardsChanged();
            }

            @Override
            public void onMinusClick(Card card) {
                activityViewModel.reduceCard(card);
                mListener.onDeckCardsChanged();
            }
        }, false, AppManager.getInstance().getSharedPrefs().getBoolean(SettingsActivity.KEY_PREF_DISPLAY_SET_NAMES_WITH_CARDS, false));

        lstDeckCards.setAdapter(mDeckCardsAdapter);
        lstDeckCards.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // View the card
                currentCard = (Card) mDeckCardsAdapter.getChild(groupPosition, childPosition);
                Intent intent = new Intent(getActivity(), ViewDeckFullscreenActivity.class);
                intent.putExtra(ViewDeckFullscreenActivity.EXTRA_CARD_CODE, currentCard.getCode());
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        mDeckCardsAdapter.filterData("");
        return true;
    }

    private void refreshCards(ArrayList<String> headers, Deck deck) {
        // Get the cards
        mListCards.clear();
        mListCards.putAll(activityViewModel.getGroupedCards(deck, headers));
        if (sv != null) {
            CharSequence query = sv.getQuery();
            if (query.length() > 0){
                mDeckCardsAdapter.filterData(query.toString());
            }
        }
        mDeckCardsAdapter.notifyDataSetChanged();
    }

    public void onFormatChanged() {
        setListView(mDeck);
    }

    public void onDeckCardsChanged() {
        refreshCards(mHeaders, mDeck);
    }
}
