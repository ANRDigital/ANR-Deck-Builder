package com.shuneault.netrunnerdeckbuilder.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.adapters.CheckedCardListAdapter;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardCount;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;

import java.util.ArrayList;
import java.util.Collections;

public class DeckBuildFragment extends Fragment implements OnDeckChangedListener {

    private LinearLayout mainView;
    private ListView lstCardsToAdd;
    private ListView lstCardsToRemove;
    private Button btnClearAll;

    private Deck mDeck;
    private ArrayList<CardCount> mCardsToAdd;
    private ArrayList<CardCount> mCardsToRemove;

    // Adapters
    private CheckedCardListAdapter mCardsToAddAdapter;
    private CheckedCardListAdapter mCardsToRemoveAdapter;

    public static final String ARGUMENT_DECK_ID = "com.example.netrunnerdeckbuilder.ARGUMENT_DECK_ID";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The View
        mainView = (LinearLayout) inflater.inflate(R.layout.fragment_deck_build, container, false);

        // The GUI
        lstCardsToAdd = (ListView) mainView.findViewById(R.id.lstCardsToAdd);
        lstCardsToRemove = (ListView) mainView.findViewById(R.id.lstCardsToRemove);
        btnClearAll = (Button) mainView.findViewById(R.id.btnClearAll);

        // Get the card lists
        mDeck = AppManager.getInstance().getDeck(getArguments().getLong(ARGUMENT_DECK_ID));
        mCardsToAdd = mDeck.getCardsToAdd();
        mCardsToRemove = mDeck.getCardsToRemove();
        Collections.sort(mCardsToAdd, new Sorter.CardCountSorterByName());
        Collections.sort(mCardsToRemove, new Sorter.CardCountSorterByName());

        // Adapters
        mCardsToAddAdapter = new CheckedCardListAdapter(getActivity(), mCardsToAdd);
        mCardsToRemoveAdapter = new CheckedCardListAdapter(getActivity(), mCardsToRemove);

        // Assign to the lists
        lstCardsToAdd.setAdapter(mCardsToAddAdapter);
        lstCardsToRemove.setAdapter(mCardsToRemoveAdapter);

        // Listeners
        lstCardsToRemove.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                CheckBox chk = (CheckBox) arg1.findViewById(R.id.chkChecked);
                chk.setChecked(!chk.isChecked());
                mCardsToRemove.get(arg2).setDone(chk.isChecked());
            }
        });
        lstCardsToAdd.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                CheckBox chk = (CheckBox) arg1.findViewById(R.id.chkChecked);
                chk.setChecked(!chk.isChecked());
                mCardsToAdd.get(arg2).setDone(chk.isChecked());
            }
        });
        btnClearAll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Ask if we are sure to clear everything
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.message_clear_checked_cards);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeck.clearCardsToAddAndRemove(true);
                        mCardsToAddAdapter.notifyDataSetChanged();
                        mCardsToRemoveAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });


        return mainView;
    }

    @Override
    public void onDeckNameChanged(Deck deck, String name) {
        //

    }

    @Override
    public void onDeckDeleted(Deck deck) {
        //

    }

    @Override
    public void onDeckCardsChanged() {
        if (!isAdded()) return;
        Collections.sort(mCardsToAdd, new Sorter.CardCountSorterByName());
        Collections.sort(mCardsToRemove, new Sorter.CardCountSorterByName());

        mCardsToAddAdapter.notifyDataSetChanged();
        mCardsToRemoveAdapter.notifyDataSetChanged();

    }

    @Override
    public void onDeckCloned(Deck deck) {
        //

    }

    @Override
    public void onDeckIdentityChanged(Card newIdentity) {
        //

    }

    @Override
    public void onSettingsChanged() {

    }

}
