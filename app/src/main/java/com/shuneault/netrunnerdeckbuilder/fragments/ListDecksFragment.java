package com.shuneault.netrunnerdeckbuilder.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shuneault.netrunnerdeckbuilder.DeckActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.ViewModel.MainActivityViewModel;
import com.shuneault.netrunnerdeckbuilder.adapters.ListDecksAdapter;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;

import java.util.ArrayList;
import java.util.Collections;

import static org.koin.java.standalone.KoinJavaComponent.get;

/**
 * Created by sebast on 11/02/16.
 */
public class ListDecksFragment extends Fragment {

    public interface OnListDecksFragmentListener {
        void OnScrollListener(RecyclerView recyclerView, int dx, int dy);
    }

    private static final String EXTRA_SIDE = "com.shuneault.netrunnerdeckbuilder.EXTRA_SIDE";

    private RecyclerView mRecyclerView;
    private OnListDecksFragmentListener mListener;

    // Database and decks
    private MainActivityViewModel viewModel = get(MainActivityViewModel.class);

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
        View mainView = inflater.inflate(R.layout.fragment_list_decks, container, false);

        // GUI
        mRecyclerView = mainView.findViewById(R.id.recyclerView);

        // Side
        mSide = getArguments().getString(EXTRA_SIDE);

        // Initialize the layout manager and adapter
        final ArrayList<Deck> mCurrentDecks = getCurrentDecks();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        ListDecksAdapter mDeckAdapter = new ListDecksAdapter(mCurrentDecks, new ListDecksAdapter.DeckViewHolder.IViewHolderClicks() {
            @Override
            public void onDeckClick(Deck deck) {
                // Load the deck activity
                if (!deck.hasUnknownCards())
                    startDeckActivity(deck.getRowId());
            }

            @Override
            public void onDeckStarred(Deck deck, boolean isStarred) {
                deck.setStarred(isStarred);
                DatabaseHelper mDb;
                mDb = AppManager.getInstance().getDatabase();
                mDb.updateDeck(deck);
                // Sort for new starred order
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
        ArrayList<Deck> mDecks = viewModel.getDecks();
        for (Deck deck : mDecks) {
            if (deck != null && deck.getSide().equals(mSide)) {
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
        ((ListDecksAdapter) mRecyclerView.getAdapter()).setData(getCurrentDecks());
    }

}
