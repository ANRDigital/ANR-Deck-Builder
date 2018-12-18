package com.shuneault.netrunnerdeckbuilder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.ViewDeckFullscreenActivity;
import com.shuneault.netrunnerdeckbuilder.adapters.HandCardsListAdapter;
import com.shuneault.netrunnerdeckbuilder.game.Card;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.Nullable;

public class DeckHandFragment extends DeckActivityFragment {

    // GUI
    private ListView lstCards;
    private Button btnNewHand;
    private Button btnDraw;

    // Cards
    private Card[] mCards;
    private HandCardsListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.fragment_deck_hand, container, false);

        // GUI
        lstCards = theView.findViewById(R.id.lstCards);
        btnNewHand = theView.findViewById(R.id.btnNewHand);
        btnDraw = theView.findViewById(R.id.btnDraw);

        // List
        mAdapter = new HandCardsListAdapter(getActivity(), new ArrayList<Card>());
        lstCards.setAdapter(mAdapter);

        // Handle the clicks
        btnNewHand.setOnClickListener(arg0 -> doNewHand());
        btnDraw.setOnClickListener(arg0 -> doDraw());
        lstCards.setOnItemClickListener((adapterView, view, i, l) -> doFullscreenCard(adapterView, i));

        return theView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Arguments
        mCards = new Card[mDeck.getDeckSize()];
        int num = 0;
        for (int i = 0; i < mDeck.getCards().size(); i++) {
            Card card = mDeck.getCards().get(i);
            for (int j = 0; j < mDeck.getCardCount(card); j++) {
                mCards[num++] = card;
            }
        }

        // Disable the btnDraw if necessary
        btnDraw.setEnabled(mCards.length > 0);
    }

    private void doNewHand() {
        // Clear and generate 5 new cards (or 9 for Andromeda identity
        int handSize = 5;
        if (mDeck.getIdentity().getCode().equals(Card.SpecialCards.CARD_ANDROMEDA))
            handSize = 9;
        mAdapter.clear();
        // Shuffle
        shuffleArray(mCards);
        // Display the first hand
        for (int i = 0; i < Math.min(handSize, mCards.length); i++) {
            mAdapter.add(mCards[i]);
        }
        // Disable the btnDraw if necessary
        disableDrawOnLimit();
    }

    private void doDraw() {
        // Add a new card
        mAdapter.add(mCards[mAdapter.getCount()]);
        // Disable the btnDraw if necessary
        disableDrawOnLimit();
    }

    private void disableDrawOnLimit() {
        btnDraw.setEnabled(mAdapter.getCount() < mCards.length);
    }

    private void doFullscreenCard(AdapterView<?> adapterView, int i) {
        Card card = (Card) adapterView.getAdapter().getItem(i);
        Intent intent = new Intent(getActivity(), ViewDeckFullscreenActivity.class);
        intent.putExtra(ViewDeckFullscreenActivity.EXTRA_CARD_CODE, card.getCode());
        startActivity(intent);
    }

    // Implementing Fisher-Yates shuffle
    private void shuffleArray(Card[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Card a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}
