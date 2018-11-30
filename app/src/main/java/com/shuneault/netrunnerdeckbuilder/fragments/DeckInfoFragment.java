package com.shuneault.netrunnerdeckbuilder.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.DeckActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;

public class DeckInfoFragment extends Fragment implements OnDeckChangedListener {

    // Listener
    OnDeckChangedListener mListener;

    View mainView;
    TextView txtDeckName;
    TextView txtDeckDescription;
    ImageView imgIdentity;
    Deck mDeck;

    // Database
    DatabaseHelper mDb;
    private TextView lblMwlVersion;
    private TextView lblMwlValid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Main View
        mainView = (View) inflater.inflate(R.layout.fragment_deck_info, container, false);

        // Arguments
        Bundle bundle = getArguments();
        mDeck = AppManager.getInstance().getDeck(bundle.getLong(DeckActivity.ARGUMENT_DECK_ID));

        // GUI
        imgIdentity = (ImageView) mainView.findViewById(R.id.imgIdentity);
        txtDeckName = (TextView) mainView.findViewById(R.id.lblLabel);
        txtDeckDescription = (TextView) mainView.findViewById(R.id.txtDeckDescription);
        lblMwlVersion = mainView.findViewById(R.id.lblMwlVersion);
        lblMwlValid = mainView.findViewById(R.id.lblMwlValid);

        // Variables
        mDb = new DatabaseHelper(getActivity());

        // Set the info
        ImageDisplayer.fill(imgIdentity, mDeck.getIdentity(), getActivity());
        txtDeckName.setText(mDeck.getName());
        txtDeckDescription.setText(mDeck.getNotes());
        lblMwlVersion.setText(mDeck.getCardPool().getMwl().getName());
        onValidation(bundle.getBoolean(DeckActivity.ARGUMENT_MWL_VALID));

        // Events
        txtDeckName.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                mDeck.setName(arg0.toString());
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(arg0.toString());
            }

        });
        txtDeckDescription.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mDeck.setNotes(s.toString());
                //mDb.updateDeck(mDeck);
            }
        });

        return mainView;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDeckChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDeckChangedListener");
        }
    }

    @Override
    public void onDeckCardsChanged() {
        //

    }

    @Override
    public void onDeckIdentityChanged(Card newIdentity) {
        if (!isAdded()) return;
        // Update the image
        if (getActivity() != null)
            imgIdentity.setImageBitmap(mDeck.getIdentity().getImage(getActivity()));
    }

    public void onValidation(boolean valid) {
        if (valid) {
            lblMwlValid.setTextAppearance(getContext(), R.style.InfoBarGood);
            lblMwlValid.setText("✓");
        } else {
            lblMwlValid.setTextAppearance(getContext(), R.style.InfoBarBad);
            lblMwlValid.setText("✗");
        }
    }
}
