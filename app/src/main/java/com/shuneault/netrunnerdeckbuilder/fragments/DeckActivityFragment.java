package com.shuneault.netrunnerdeckbuilder.fragments;

import android.content.Context;
import android.os.Bundle;

import com.shuneault.netrunnerdeckbuilder.ViewModel.DeckActivityViewModel;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.interfaces.IDeckViewModelProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public abstract class DeckActivityFragment extends Fragment {
    protected Deck mDeck;
    protected DeckActivityViewModel activityViewModel;
    protected IDeckViewModelProvider mViewModelProvider;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activityViewModel = mViewModelProvider.getViewModel();

        mDeck = activityViewModel.getDeck();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        FragmentActivity activity = getActivity();
        try {
            mViewModelProvider = (IDeckViewModelProvider) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement IDeckViewModelProvider");
        }
    }
}
