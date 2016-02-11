package com.shuneault.netrunnerdeckbuilder.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.DeckActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.adapters.CardDeckAdapter;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by sebast on 11/02/16.
 */
public class ListDecksFragment extends Fragment {

    public interface OnListDecksFragmentListener {
        void OnScrollListener(RecyclerView recyclerView, int dx, int dy);
    }

    public static final String EXTRA_SIDE = "com.shuneault.netrunnerdeckbuilder.EXTRA_SIDE";

    private View mainView;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CardDeckAdapter mDeckAdapter;
    private OnListDecksFragmentListener mListener;

    // Database and decks
    DatabaseHelper mDb;
    ArrayList<Deck> mDecks;

    // Intent information
    private String mSide;

    public static ListDecksFragment newInstance(String side) {
        ListDecksFragment fragment = new ListDecksFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SIDE, side);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_list_decks, container, false);

        // GUI
        mRecyclerView = (RecyclerView) mainView.findViewById(R.id.recyclerView);

        // Side
        mSide = getArguments().getString(EXTRA_SIDE);

        // Some variables
        mDb = AppManager.getInstance().getDatabase();
        mDecks = AppManager.getInstance().getAllDecks();

        // Initialize the layout manager and adapter
        final ArrayList<Deck> mCurrentDecks = getCurrentDecks();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mDeckAdapter = new CardDeckAdapter(mCurrentDecks, new CardDeckAdapter.ViewHolder.IViewHolderClicks() {
            @Override
            public void onClick(int index) {
                Deck deck = mCurrentDecks.get(index);
                // Load the deck activity
                startDeckActivity(deck.getRowId());
            }

            @Override
            public void onDeckStarred(int index, boolean isStarred) {
                Deck deck = mCurrentDecks.get(index);
                deck.setStarred(isStarred);
                mDb.updateDeck(deck);
                // Sort
                Collections.sort(mCurrentDecks, new Sorter.DeckSorter());
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });

        // Initialize the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mDeckAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mListener.OnScrollListener(recyclerView, dx, dy);
            }
        });

        return mainView;
    }

    private ArrayList<Deck> getCurrentDecks() {
        // Only the selected tab decks
        final ArrayList<Deck> mCurrentDecks = new ArrayList<>();
        for (Deck deck : mDecks) {
            if (deck.getSide().equals(mSide)) {
                mCurrentDecks.add(deck);
            }
        }
        // Sort the list
        Collections.sort(mCurrentDecks, new Sorter.DeckSorter());
        return mCurrentDecks;
    }

    private void startDeckActivity(Long rowId) {
        Intent intent = new Intent(getActivity(), DeckActivity.class);
        intent.putExtra(DeckActivity.ARGUMENT_DECK_ID, rowId);
        getActivity().startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnListDecksFragmentListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnListDecksFragmentListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((CardDeckAdapter) mRecyclerView.getAdapter()).setData(getCurrentDecks());
    }

}
