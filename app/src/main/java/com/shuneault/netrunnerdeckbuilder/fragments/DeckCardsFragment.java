package com.shuneault.netrunnerdeckbuilder.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shuneault.netrunnerdeckbuilder.DeckActivity;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DeckCardsFragment extends Fragment implements OnDeckChangedListener, MenuItemCompat.OnActionExpandListener {

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
        mDeck = AppManager.getInstance().getDeck(getArguments().getLong(DeckActivity.ARGUMENT_DECK_ID));

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
        SearchView sv = new SearchView(getActivity());
        item.setShowAsAction(MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        item.setActionView(sv);

        //Applies white color on searchview text
	    SearchView.SearchAutoComplete searchAutoComplete =
			    (SearchView.SearchAutoComplete) sv.findViewById(android.support.v7.appcompat.R.id.search_src_text);
	    searchAutoComplete.setHintTextColor(Color.WHITE);
	    searchAutoComplete.setTextColor(Color.WHITE);
	    try {
		    Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
		    mCursorDrawableRes.setAccessible(true);
		    mCursorDrawableRes.set(searchAutoComplete, 0); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
	    } catch (Exception e) {}

	    // set close button to white x
	    ImageView searchCloseIcon = (ImageView) sv.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
	    searchCloseIcon.setImageDrawable(getResources().getDrawable(R.drawable.abc_ic_clear_mtrl_alpha));

	    // remove search icon
	    sv.setIconifiedByDefault(false);
	    ImageView searchIcon = (ImageView) sv.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
	    searchIcon.setLayoutParams(new LinearLayout.LayoutParams(0,0));

	    // set underline
	    View searchPlate = sv.findViewById(android.support.v7.appcompat.R.id.search_plate);
	    searchPlate.setBackgroundResource(R.drawable.search);

		sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String arg0) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String arg0) {
                if (mDeckCardsAdapter != null) {
                    mDeckCardsAdapter.filterData(arg0);
                }
                return false;
			}
		});
        MenuItemCompat.setOnActionExpandListener(item, this);
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
            // Cannot add Jinteki card for "Custom Biotics: Engineered for Success" Identity
            boolean isJintekiOK = !theCard.getFactionCode().equals(Card.Faction.FACTION_JINTEKI) || !mDeck.getIdentity().getCode().equals(Card.SpecialCards.CARD_CUSTOM_BIOTICS_ENGINEERED_FOR_SUCCESS);
            // Ignore non-virtual resources if runner is Apex and setting is set
            boolean isNonVirtualOK = !theCard.getType().contains("Resource") || (theCard.getType().contains("Resource") && theCard.getSubtype().contains("Virtual") && mDeck.getIdentity().getCode().equals(Card.SpecialCards.APEX) && PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pref_HideNonVirtualApex", true));
            if (isSameSide && !isIdentity && isGoodAgenda && isJintekiOK && isNonVirtualOK) {
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

	@Override
	public boolean onMenuItemActionExpand(MenuItem item)
	{
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item)
	{
		mDeckCardsAdapter.filterData("");
		return true;
	}
}
