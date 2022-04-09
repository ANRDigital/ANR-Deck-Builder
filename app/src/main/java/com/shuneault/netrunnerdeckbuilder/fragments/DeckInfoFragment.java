package com.shuneault.netrunnerdeckbuilder.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.ViewDeckFullscreenActivity;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;
import com.shuneault.netrunnerdeckbuilder.interfaces.ValidityProvider;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.FragmentActivity;
import kotlin.Lazy;

import static org.koin.java.KoinJavaComponent.inject;

public class DeckInfoFragment extends DeckActivityFragment {

    // Listener
    private OnDeckChangedListener mListener;

    private TextView txtDeckName;
    private TextView txtDeckDescription;
    private ImageView imgIdentity;

    private TextView lblMwlVersion;
    private AppCompatSpinner spnFormat;
    private final Lazy<CardRepository> cardRepo = inject(CardRepository.class);

    private ArrayAdapter<Format> mFormatAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Main View
        View mainView = inflater.inflate(R.layout.fragment_deck_info, container, false);

        // GUI
        imgIdentity = mainView.findViewById(R.id.imgIdentity);
        imgIdentity.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ViewDeckFullscreenActivity.class);
            intent.putExtra(ViewDeckFullscreenActivity.EXTRA_CARD_CODE, mDeck.getIdentity().getCode());
            startActivity(intent);
        });

        txtDeckName = mainView.findViewById(R.id.lblLabel);
        txtDeckDescription = mainView.findViewById(R.id.txtDeckDescription);
        lblMwlVersion = mainView.findViewById(R.id.lblMwlVersion);
        spnFormat = mainView.findViewById(R.id.spnFormat);
        ArrayList<Format> formats = cardRepo.getValue().getFormats();
        mFormatAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, formats);
        spnFormat.setAdapter(mFormatAdapter);
        spnFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // update the deck format
                Format item = mFormatAdapter.getItem(position);
                mListener.onFormatChanged(item);
                updateMwlDisplay(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Events
        txtDeckName.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                activityViewModel.setDeckName(arg0.toString());
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(arg0.toString());

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

        });
        txtDeckDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                activityViewModel.setDeckDescription(s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        return mainView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set the info
        ImageDisplayer.fill(imgIdentity, mDeck.getIdentity(), getActivity());
        txtDeckName.setText(mDeck.getName());
        txtDeckDescription.setText(Html.fromHtml(mDeck.getNotes()));
        Format deckFormat = mDeck.getFormat();
        spnFormat.setSelection(mFormatAdapter.getPosition(deckFormat));
        updateMwlDisplay(deckFormat);
    }

    private void updateMwlDisplay(Format deckFormat) {
        int mwlId = deckFormat.getMwlId();
        MostWantedList mwl = cardRepo.getValue().getMostWantedList(mwlId);
        lblMwlVersion.setText(mwl == null ? "No MWL" : mwl.getName());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        FragmentActivity activity = getActivity();
        try {
            mListener = (OnDeckChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDeckChangedListener");
        }
    }

}
