package com.shuneault.netrunnerdeckbuilder.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.shuneault.netrunnerdeckbuilder.MainActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.ViewDeckFullscreenActivity;
import com.shuneault.netrunnerdeckbuilder.adapters.ExpandableDeckCardListAdapter;
import com.shuneault.netrunnerdeckbuilder.adapters.ExpandableDeckCardListAdapter.OnButtonClickListener;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;

public class DeckCardsFragment extends Fragment implements OnDeckChangedListener {
	
	// Saved Instance
	private static final String SAVED_SELECTED_CARD_CODE = "CARD_CODE";
	
	private OnDeckChangedListener mListener;
	
	private Deck mDeck;

	private Card currentCard;
	private View mainView;
	private ExpandableListView lstDeckCards;
	private ExpandableDeckCardListAdapter mDeckCardsAdapter;
	
	// Database
	DatabaseHelper mDb;
	
	private ArrayList<String> mListHeaders;
	private HashMap<String, ArrayList<Card>> mListCards;

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
		
		// Database
		mDb = new DatabaseHelper(getActivity());

		// Set the list view
		setListView();
		
		return mainView;

	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Do not inflate if already there
		if (menu.findItem(R.id.mnuSearch) == null)
			inflater.inflate(R.menu.deck_cards, menu);
		MenuItem item = menu.findItem(R.id.mnuSearch);
		SearchView sv = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
		MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		MenuItemCompat.setActionView(item, sv);
		sv.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String arg0) {
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String arg0) {
				mDeckCardsAdapter.filterData(arg0);
				return false;
			}
		});
		MenuItemCompat.setOnActionExpandListener(item, new OnActionExpandListener() {
			
			@Override
			public boolean onMenuItemActionExpand(MenuItem arg0) {
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem arg0) {
				mDeckCardsAdapter.filterData("");
				return true;
			}
		});
		super.onCreateOptionsMenu(menu, inflater);
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
	
	private void setListView() {
		// Get the headers
		mListHeaders = AppManager.getInstance().getAllCards().getCardType(mDeck.getIdentity().getSide());
		mListHeaders.remove(mDeck.getIdentity().getType()); // Remove the Identity category
		Collections.sort(mListHeaders);
		
		// Get the cards
		mListCards = new HashMap<String, ArrayList<Card>>();
		for (Card theCard : AppManager.getInstance().getCardsFromDataPacksToDisplay()) {
			// Only add the cards that are on my side
			boolean isSameSide = theCard.getSide().equals(mDeck.getIdentity().getSide());
			// Do not add the identities
			boolean isIdentity = theCard.getTypeCode().equals(Card.Type.IDENTITY);
			// Only display agendas that belong to neutral or my faction
			boolean isGoodAgenda = !theCard.getTypeCode().equals(Card.Type.AGENDA) || theCard.getFaction().equals(mDeck.getIdentity().getFaction()) || theCard.getFactionCode().equals(Card.Faction.FACTION_NEUTRAL);
			if (isSameSide && !isIdentity && isGoodAgenda) {
				if (mListCards.get(theCard.getType()) == null)
					mListCards.put(theCard.getType(), new ArrayList<Card>());
				mListCards.get(theCard.getType()).add(theCard);
			}
		}
		
		// Sort the cards
		sortListCards();
		
		// Set the adapter
		mDeckCardsAdapter = new ExpandableDeckCardListAdapter(getActivity(), mListHeaders, mListCards, mDeck, new OnButtonClickListener() {
			
			@Override
			public void onPlusClick(Card card) {
				mListener.onDeckCardsChanged();
			}
			
			@Override
			public void onMinusClick(Card card) {
				mListener.onDeckCardsChanged();
			}
		});
		lstDeckCards.setAdapter(mDeckCardsAdapter);
		lstDeckCards.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// View the card
				currentCard = (Card) mDeckCardsAdapter.getChild(groupPosition, childPosition);
				Intent intent = new Intent(getActivity(), ViewDeckFullscreenActivity.class);
				intent.putExtra(ViewDeckFullscreenActivity.EXTRA_CARD_CODE, currentCard.getCode());
				startActivity(intent);
				return false;
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Refresh the cards adapter as the changed preferences may have changed
		//setListView();
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
		if (!isAdded()) return;
		setListView();
	}

	@Override
	public void onSettingsChanged() {
		// Refresh the cards
		if (!isAdded()) return;
		setListView();
	}
	
}
