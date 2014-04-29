package com.shuneault.netrunnerdeckbuilder.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Spinner;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.ViewImageActivity;
import com.shuneault.netrunnerdeckbuilder.adapters.ExpandableDeckCardListAdapter;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;
import com.shuneault.netrunnerdeckbuilder.prefs.SetNamesPreferenceMultiSelect;

public class DeckCardsFragment extends Fragment implements OnDeckChangedListener {
	
	// Saved Instance
	private static final String SAVED_SELECTED_CARD_CODE = "CARD_CODE";
	
	private OnDeckChangedListener mListener;
	
	private Deck mDeck;

	private Card currentCard;
	private View mainView;
	private ExpandableListView lstDeckCards;
	private ImageView imgCardInfo;
	private NumberPicker pickNumCards;
	private Spinner spinNumCards;
	private ExpandableDeckCardListAdapter mDeckCardsAdapter;
	
	// Database
	DatabaseHelper mDb;
	
	// Sets to display preference
	private String[] mSetsToDisplay;
	
	private ArrayList<String> mListHeaders;
	private HashMap<String, ArrayList<Card>> mListCards;

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// 
		setHasOptionsMenu(true);
		
		// Inflate
		mainView = inflater.inflate(R.layout.fragment_deck_cards, container, false);
		
		// Get the arguments
		mDeck = AppManager.getInstance().getDeck(getArguments().getLong(DeckFragment.ARGUMENT_DECK_ID));
		
		// The GUI items
		lstDeckCards = (ExpandableListView) mainView.findViewById(R.id.lstDeckCards);
		imgCardInfo = (ImageView) mainView.findViewById(R.id.imgCardInfo);
		pickNumCards = (NumberPicker) mainView.findViewById(R.id.pickNumCards);
		spinNumCards = (Spinner) mainView.findViewById(R.id.spinNumCards);
		
		// Database
		mDb = new DatabaseHelper(getActivity());
		
		// The sets to display
		mSetsToDisplay = SetNamesPreferenceMultiSelect.parseStoredValue(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SettingsActivity.KEY_PREF_DATA_PACKS_TO_DISPLAY, ""));

		// Number Picker (v14)
		if (pickNumCards != null) {
			pickNumCards.setMinValue(0);
			pickNumCards.setMaxValue(Deck.MAX_INDIVIDUAL_CARD);
			pickNumCards.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // Prevent number input using keyboard
			pickNumCards.setOnValueChangedListener(new OnValueChangeListener() {
				
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					// Add the card to the deck
					mDeck.setCardCount(currentCard, newVal);
									
					// Update the display
					refreshDisplay();
					
					// OnDeckChanged Listener
					mListener.onDeckCardsChanged();
				}
			});
		} else {
			// Add 0-3 choices
			ArrayList<String> arrNum = new ArrayList<String>();
			for (int i = 0; i <= Deck.MAX_INDIVIDUAL_CARD; i++)
				arrNum.add(String.valueOf(i));
			spinNumCards.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, arrNum));
			
			spinNumCards.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// Add the card to the deck
					mDeck.setCardCount(currentCard, arg2);
									
					// Update the display
					refreshDisplay();
					
					// OnDeckChanged Listener
					mListener.onDeckCardsChanged();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// 
					
				}
			});
		}
		
		// Set the list view
		setListView();

		// Display the card if resume
		if (savedInstanceState != null) {
			displayCard(AppManager.getInstance().getCard(savedInstanceState.getString(SAVED_SELECTED_CARD_CODE)));
		}
		
		// Image Click - Display the image bigger
		imgCardInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ViewImageActivity.class);
				intent.putExtra(ViewImageActivity.EXTRA_CARD_CODE, currentCard.getCode());
				startActivity(intent);
			}
		});
		
		return mainView;

	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Do not inflate if already there
		super.onCreateOptionsMenu(menu, inflater);
//		if (menu.findItem(R.id.mnuInfoBar) != null)
//			inflater.inflate(R.menu.deck_cards, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 
		switch (item.getItemId()) {

		default:
			return super.onOptionsItemSelected(item);
		}
		
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Remember the selected card
		if (currentCard != null)
			outState.putString(SAVED_SELECTED_CARD_CODE, currentCard.getCode());
	}
	
	

	private void sortListCards() {
		// Sort by faction,
		// My cards must be sorted by type
		String strMyCards = getResources().getString(R.string.my_cards);
		for (String strCat : mListHeaders) {
			if (mListCards.get(strCat) == null) continue;
			if (strCat.equals(strMyCards))
				Collections.sort(mListCards.get(strCat), new Sorter.CardSorterByCardType());		
			else
				//Collections.sort(mListCards.get(strCat), new Sorter.CardSorterByFaction());
				Collections.sort(mListCards.get(strCat), new Sorter.CardSorterByFactionWithMineFirst(mDeck.getIdentity()));
		}
		
	}
	
	@SuppressLint("NewApi")
	private void displayCard(Card card) {
		if (card == null) return;
		
		// Change the current card
		currentCard = card;
		ImageDisplayer.fill(imgCardInfo, currentCard, getActivity());
		
		// Display the image
		imgCardInfo.setVisibility(View.VISIBLE);
		
		// Show the number picker or spinner
		if (pickNumCards != null) {
			pickNumCards.setValue(mDeck.getCardCount(currentCard));
			pickNumCards.setVisibility(View.VISIBLE);
		} else {
			spinNumCards.setSelection(mDeck.getCardCount(currentCard));
			spinNumCards.setVisibility(View.VISIBLE);
		}
	}
	
	private void refreshDisplay() {
		// Update the adapters
		mDeckCardsAdapter.notifyDataSetChanged();
	}
	
	private void setListView() {
		// Get the headers
		mListHeaders = AppManager.getInstance().getAllCards().getCardType(mDeck.getIdentity().getSide());
		mListHeaders.remove(mDeck.getIdentity().getType()); // Remove the Identity category
		Collections.sort(mListHeaders);
		
		// Get the cards
		mListCards = new HashMap<String, ArrayList<Card>>();
		for (Card theCard : AppManager.getInstance().getCardsFromDataPacksToDisplay()) {
			// Only add the cards that are on my side
			// Do not add the identities
			if (!theCard.getTypeCode().equals(Card.Type.IDENTITY)
					&& theCard.getSide().equals(mDeck.getIdentity().getSide())) {
				if (mListCards.get(theCard.getType()) == null)
					mListCards.put(theCard.getType(), new ArrayList<Card>());
				mListCards.get(theCard.getType()).add(theCard);
			}
		}
		
		// Sort the cards
		sortListCards();
		
		// Set the adapter
		mDeckCardsAdapter = new ExpandableDeckCardListAdapter(getActivity(), mListHeaders, mListCards, mDeck);
		lstDeckCards.setAdapter(mDeckCardsAdapter);
		lstDeckCards.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				// Set the card
				displayCard(mListCards.get(mListHeaders.get(groupPosition)).get(childPosition));

				return true;
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Refresh the cards adapter as the changed preferences may have changed
		setListView();
	}

	@Override
	public void onDeckNameChanged(Deck deck, String name) {
	}

	@Override
	public void onDeckDeleted(Deck deck) {
	}

	@Override
	public void onDeckCloned(Deck deck) {		
	}

	@Override
	public void onDeckCardsChanged() {

	}

	@Override
	public void onDeckIdentityChanged(Card newIdentity) {
		refreshDisplay();
	}
	
}
