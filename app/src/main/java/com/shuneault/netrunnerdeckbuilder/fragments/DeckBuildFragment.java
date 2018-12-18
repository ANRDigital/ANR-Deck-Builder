package com.shuneault.netrunnerdeckbuilder.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.adapters.CheckedCardListAdapter;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardCount;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.Nullable;

public class DeckBuildFragment extends DeckActivityFragment {

    private LinearLayout mainView;
    private ListView lstCardsToAdd;
    private Button btnClearAll;
    private Button btnUnbuild;

    private ArrayList<CardCount> mCardsToAdd;

    // Adapters
    private CheckedCardListAdapter mCardsToAddAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The View
        mainView = (LinearLayout) inflater.inflate(R.layout.fragment_deck_build, container, false);

        // The GUI
        lstCardsToAdd = mainView.findViewById(R.id.lstCardsToAdd);
        btnClearAll = mainView.findViewById(R.id.btnClearAll);
        btnUnbuild = mainView.findViewById(R.id.btnUnbuild);

        // Listeners
        lstCardsToAdd.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            CheckBox chk = arg1.findViewById(R.id.chkChecked);
            chk.setChecked(!chk.isChecked());
            mCardsToAdd.get(arg2).setDone(chk.isChecked());
        });
        btnClearAll.setOnClickListener(v -> doClearAll());
        btnUnbuild.setOnClickListener(v -> doUnBuild());

        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get the card lists
        mCardsToAdd = mDeck.getCardsToAdd();
        Collections.sort(mCardsToAdd, new Sorter.CardCountSorterByName());

        // Adapters
        mCardsToAddAdapter = new CheckedCardListAdapter(getActivity(), mCardsToAdd);

        // Assign to the lists
        lstCardsToAdd.setAdapter(mCardsToAddAdapter);
    }

    private void doUnBuild() {
        // Ask to make sure we want to "unbuild" the deck
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.message_unbuild));
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            mDeck.clearCardsToAddAndRemove();
            ArrayList<CardCount> arrCardCount = new ArrayList<CardCount>();
            for (Card card : mDeck.getCards()) {
                arrCardCount.add(new CardCount(card, mDeck.getCardCount(card)));
            }
            mDeck.setCardsToAdd(arrCardCount);
            mCardsToAddAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void doClearAll() {
        // Ask if we are sure to clear everything

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.message_clear_checked_cards);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            mDeck.clearCardsToAddAndRemove(true);
            mCardsToAddAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
