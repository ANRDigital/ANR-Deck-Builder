package com.shuneault.netrunnerdeckbuilder.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.ViewDeckFullscreenActivity;
import com.shuneault.netrunnerdeckbuilder.adapters.ExpandableDeckCardListAdapter;
import com.shuneault.netrunnerdeckbuilder.adapters.ExpandableDeckCardListAdapter.OnButtonClickListener;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DeckMyCardsFragment extends DeckActivityFragment {

    // Saved Instance
    private static final String SAVED_SELECTED_CARD_CODE = "CARD_CODE";

    private OnDeckChangedListener mListener;

    private Card currentCard;
    private View mainView;

    // TODO: Change to ExpandableStickyListAdapter https://github.com/emilsjolander/StickyListHeaders
    private ExpandableListView lstDeckCards;
    private ExpandableDeckCardListAdapter mDeckCardsAdapter;

    private HashMap<String, ArrayList<Card>> mListCards = new HashMap<String, ArrayList<Card>>();
    private ArrayList<String> mHeaders;

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //
        setHasOptionsMenu(true);

        // Inflate
        mainView = inflater.inflate(R.layout.fragment_deck_cards, container, false);

        // The GUI items
        lstDeckCards = mainView.findViewById(R.id.lstDeckCards);

        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set the list view
        setListView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //
        switch (item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }

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

    private void setListView() {

        mHeaders = activityViewModel.getCardHeaders();
        Deck deck = this.mDeck;
        mListCards = activityViewModel.getMyGroupedCards(mHeaders, deck);

        // Adapters
        CardRepository cardRepo = AppManager.getInstance().getCardRepository();
        mDeckCardsAdapter = new ExpandableDeckCardListAdapter(cardRepo, getActivity(), mHeaders, mListCards, this.mDeck, new OnButtonClickListener() {
            @Override
            public void onPlusClick(Card card) {
                activityViewModel.addCard(card);
                // Update the listview
                mListener.onDeckCardsChanged();
            }

            @Override
            public void onMinusClick(Card card) {
                activityViewModel.reduceCard(card);

                // Remove zero cards
                if (DeckMyCardsFragment.this.mDeck.getCardCount(card) <= 0) {
                    mListCards.get(card.getTypeCode()).remove(card);
                }
                // Update the list
                mListener.onDeckCardsChanged();
            }
        }, true, AppManager.getInstance().getSharedPrefs().getBoolean(SettingsFragment.KEY_PREF_DISPLAY_SET_NAMES_WITH_CARDS, false));
        lstDeckCards.setAdapter(mDeckCardsAdapter);
        lstDeckCards.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                currentCard = (Card) mDeckCardsAdapter.getChild(groupPosition, childPosition);
                Intent intent = new Intent(getActivity(), ViewDeckFullscreenActivity.class);
                intent.putExtra(ViewDeckFullscreenActivity.EXTRA_CARD_CODE, currentCard.getCode());
                startActivity(intent);
                return false;
            }
        });
    }

    private void refreshCards(ArrayList<String> headers, Deck deck) {
        // Get the cards
        mListCards.clear();
        mListCards.putAll(activityViewModel.getMyGroupedCards(headers, deck));
        mDeckCardsAdapter.notifyDataSetChanged();
    }

    public void onFormatChanged() {
        setListView();
    }

    public void onDeckCardsChanged() {
        refreshCards(mHeaders, mDeck);
    }
}
