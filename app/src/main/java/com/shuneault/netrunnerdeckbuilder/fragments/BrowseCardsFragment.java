package com.shuneault.netrunnerdeckbuilder.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import kotlin.Lazy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.ViewModel.BrowseCardsViewModel;
import com.shuneault.netrunnerdeckbuilder.adapters.BrowseCardRecyclerViewAdapter;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;

import java.util.ArrayList;

import static org.koin.java.standalone.KoinJavaComponent.inject;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnBrowseCardsClickListener} interface.
 */
public class BrowseCardsFragment extends Fragment implements SearchView.OnQueryTextListener{
    private OnBrowseCardsClickListener mClickListener;

    private BrowseCardRecyclerViewAdapter mAdapter;
    private String mSearchText = "";

    Lazy<BrowseCardsViewModel> viewModel = inject(BrowseCardsViewModel.class);
    Lazy<CardRepository> cardRepo = inject(CardRepository.class);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

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

            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            BrowseCardsViewModel vm = viewModel.getValue();
            vm.init();

            mAdapter = new BrowseCardRecyclerViewAdapter(vm.getCards(), mClickListener, cardRepo.getValue());
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBrowseCardsClickListener) {
            mClickListener = (OnBrowseCardsClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBrowseCardsClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClickListener = null;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        this.mSearchText = newText;
        updateResults();
        return true;
    }

    private void updateResults() {
        viewModel.getValue().doSearch(mSearchText);
        mAdapter.notifyDataSetChanged();
    }

    public void updatePackFilter(ArrayList<String> packFilter) {
        viewModel.getValue().updatePackFilter(packFilter);
        updateResults();
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
