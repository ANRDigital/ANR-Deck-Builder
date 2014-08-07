package com.shuneault.netrunnerdeckbuilder.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shuneault.netrunnerdeckbuilder.R;

public class DeckStatsFragment extends Fragment {
	
	View mainView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// The View
		mainView = inflater.inflate(R.layout.fragment_deck_stats, container, false);
		
		return mainView;
	}
}
