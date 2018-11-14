package com.shuneault.netrunnerdeckbuilder.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import java.util.List;

import javax.inject.Inject;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnBrowseCardsClickListener}
 * interface.
 */
public class BrowseCardsFragment extends Fragment implements SearchView.OnQueryTextListener{

    // TODO: Customize parameter argument names

    private OnBrowseCardsClickListener mListener;

    @Inject
    CardRepository cardRepo;
    private List<Card> mCards;
    private BrowseCardRecyclerViewAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BrowseCardsFragment() {
        cardRepo = AppManager.getInstance().getCardRepository();
    }

    public static BrowseCardsFragment newInstance() {
        BrowseCardsFragment fragment = new BrowseCardsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_cards, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            // TODO: Customize parameters
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            mCards = cardRepo.GetAllCards();

            mAdapter = new BrowseCardRecyclerViewAdapter(mCards, mListener);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBrowseCardsClickListener) {
            mListener = (OnBrowseCardsClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBrowseCardsClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mCards.clear();
        mCards.addAll(cardRepo.searchCards(newText));
        mAdapter.notifyDataSetChanged();
        return true;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnBrowseCardsClickListener {
        void onCardClicked(Card card);
    }
}
