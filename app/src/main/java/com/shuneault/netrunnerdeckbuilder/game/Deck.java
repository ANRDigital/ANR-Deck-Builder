package com.shuneault.netrunnerdeckbuilder.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.adapters.HeaderListItemInterface;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

public class Deck implements Serializable, HeaderListItemInterface {
	
	private String mName;
	private Card mIdentity;
	private String mNotes;
    private boolean mStarred;
	private UUID mUUID = UUID.randomUUID();
	private Long mRowId;
	private HashMap<Card, Integer> mCards = new HashMap<Card, Integer>();
	
	// Cards to add and cards to remove for deck building
	private HashMap<Card, CardCount> mCardsToAdd = new HashMap<Card, CardCount>();
	private HashMap<Card, CardCount> mCardsToRemove = new HashMap<Card, CardCount>();
	private ArrayList<CardCount> mArrCardsToAdd = new ArrayList<CardCount>();
	private ArrayList<CardCount> mArrCardsToRemove = new ArrayList<CardCount>();
	
	// JSON Values
	public static final String JSON_DECK_UUID = "deck_uuid";
	public static final String JSON_DECK_NAME = "deck_name";
	public static final String JSON_DECK_NOTES = "deck_notes";
	public static final String JSON_DECK_IDENTITY_CODE = "deck_identity_code";
	public static final String JSON_DECK_CARD_CODE = "card_code";
	public static final String JSON_DECK_CARD_COUNT = "card_count";
	public static final String JSON_DECK_CARDS = "cards";
	public static final String JSON_DECK_CARDS_TO_ADD = "cards_to_add";
	public static final String JSON_DECK_CARDS_TO_REMOVE = "cards_to_remove";
	public static final String JSON_DECK_CARDS_DONE = "is_done";
	
	// Rules values
	public static final int BASE_AGENDA = 2;
	public static final int MAX_INDIVIDUAL_CARD = 3;

	/**
	 * 
	 */
	private static final long serialVersionUID = 2114649051205735605L;
	
	private Deck() {
		this("", "");
	}

	public Deck(String name, Card Identity) {
		this.mName = name;
		this.mNotes = "";
		this.mIdentity = Identity;
        this.mStarred = false;
	}
	public Deck(String name, String identity_code) {
		this(name, AppManager.getInstance().getCard(identity_code));
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}
	
	public Card getIdentity() {
		return mIdentity;
	}
	
	public void setIdentity(Card card) {
		mIdentity = card;
	}
	
	public String getNotes() {
		return mNotes;
	}
	
	public void setNotes(String notes) {
		mNotes = notes;
	}
	
	public String getSide() {
		return mIdentity.getSideCode();
	}

	@Override
	public int getItemType() {
		return HeaderListItemInterface.TYPE_ITEM;
	}

	@Override
	public String getItemName() {
		return this.getName();
	}
	
	
	public void setCardCount(Card card, int count) {
		// Count must be between 0 and the maximum allowed by the game
		count = Math.max(0, count);
		count = Math.min(card.getMaxCardCount(), count);
		
		// Add or remove the card count
		int iCountToAdd = (mCardsToAdd.get(card) == null ? 0 : mCardsToAdd.get(card).getCount());
		int iCountToRemove = (mCardsToRemove.get(card) == null ? 0 : mCardsToRemove.get(card).getCount());
		int iCountOriginal = (mCards.get(card) == null ? 0 : mCards.get(card)) +-iCountToAdd + iCountToRemove;
		mCardsToAdd.remove(card);
		mCardsToRemove.remove(card);
		if (iCountOriginal != count) {
			// Add or remove
			if (iCountOriginal > count) // We removed some cards
				mCardsToRemove.put(card, new CardCount(card, iCountOriginal-count));
			else
				mCardsToAdd.put(card, new CardCount(card, count-iCountOriginal));
		}
		
		// Regenerate the arrays
		getCardsToAdd();
		getCardsToRemove();
		
		// Modify the deck
		mCards.remove(card);
		if (count > 0)
			mCards.put(card, count);
		
	}
	
	public ArrayList<Card> getCards() {
		ArrayList<Card> cardList = new ArrayList<Card>();
		for (Card card : mCards.keySet())
			cardList.add(card);
		return cardList;
	}
	
	public int getCardCount(Card card) {
		Integer iCount = mCards.get(card);
		if (iCount == null)
			return 0;
		else
			return iCount;
	}

    public int getCardCountByType(String type) {
        int iCount = 0;
        for (Card card : mCards.keySet()) {
            if (card.getType().equals(type)) {
                iCount = iCount + getCardCount(card);
            }
        }
        return iCount;
    }
	
	public ArrayList<CardCount> getCardsToAdd() {
		mArrCardsToAdd.clear();
		for (Card card : mCardsToAdd.keySet())
			mArrCardsToAdd.add(mCardsToAdd.get(card));
		return mArrCardsToAdd;
	}

	public ArrayList<CardCount> getCardsToRemove() {
		mArrCardsToRemove.clear();
		for (Card card : mCardsToRemove.keySet())
			mArrCardsToRemove.add(mCardsToRemove.get(card));
		return mArrCardsToRemove;
	}
	
	public int getCardsToAddCount(Card card) {
		Integer iCount = mCardsToAdd.get(card).getCount();
		return (iCount == null ? 0 : iCount);
	}

	public int getCardsToRemoveCount(Card card) {
		Integer iCount = mCardsToRemove.get(card).getCount();
		return (iCount == null ? 0 : iCount);
	}
	
	public int getDeckSize() {
		int iDeckSize = 0;
		for (Card card : getCards()) {
			iDeckSize = iDeckSize + getCardCount(card);
		}
		return iDeckSize;
	}
	
	public int getDeckInfluence() {
		int iInfluence = 0;
		// IDENTITY: The Professor (03029) does count influence diffently
		if (mIdentity.getCode().equals(Card.SpecialCards.CARD_THE_PROCESSOR)) {
			for (Card card : getCards()) {
				if (!mIdentity.getFaction().equals(card.getFaction())) {
					if (card.getTypeCode().equals(Card.Type.PROGRAM)) {
						// First copy of each program does not count toward the influence value
						iInfluence = iInfluence + (card.getFactionCost() * Math.max(getCardCount(card)-1, 0));
					} else {
						iInfluence = iInfluence + (card.getFactionCost() * getCardCount(card));
					}
				}
			}
		} else {
			for (Card card : getCards()) {
				if (!mIdentity.getFaction().equals(card.getFaction())) {
					iInfluence = iInfluence + (card.getFactionCost() * getCardCount(card));
				}
			}
		}
		return iInfluence;
	}
	
	public int getMinimumDeckSize() {
		return mIdentity.getMinimumDeckSize();
	}
	
	public int getInfluenceLimit() {
		return mIdentity.getInfluenceLimit();
	}
	
	public int getDeckAgenda() {
		int iAgendaPoints = 0;
		for (Card card : getCards()) {
			iAgendaPoints = iAgendaPoints + (card.getAgendaPoints() * getCardCount(card));
		}
		return iAgendaPoints;
	}
	
	// Returns the maximum between the minimum agenda based on min deck size
	//	or the agenda requirement based on the current deck size
	public int getDeckAgendaMinimum() {
		// Calculation: BASE_AGENDA + (floor(CardCount)/5*2)
		return (int) (BASE_AGENDA + (Math.floor(Math.max(getDeckSize(),getMinimumDeckSize())/5)*2));
	}
	
	public void clearCardsToAddAndRemove() {
		mCardsToAdd.clear();
		mCardsToRemove.clear();
		mArrCardsToAdd.clear();
		mArrCardsToRemove.clear();
	}
	
	public void clearCardsToAddAndRemove(boolean onlyChecked) {
		if (!onlyChecked) {
			clearCardsToAddAndRemove();
		} else {
			// Cards to add
			Iterator<CardCount> it = mArrCardsToAdd.iterator();
			while (it.hasNext()) {
				CardCount cc = it.next();
				if (cc.isDone()) {
					it.remove();
					mCardsToAdd.remove(cc.getCard());
				}
			}
			// Cards to remove
			it = mArrCardsToRemove.iterator();
			while (it.hasNext()) {
				CardCount cc = it.next();
				if (cc.isDone()) {
					it.remove();
					mCardsToRemove.remove(cc.getCard());
				}
			}
		}
	}
	
	public boolean isInfluenceOk() {
		return (getDeckInfluence() <= getInfluenceLimit());
	}
	
	public boolean isCardCountOk() {
		return (getDeckSize() >= getMinimumDeckSize());
	}
	
	public boolean isAgendaOk() {
		return (getSide().equals(Card.Side.SIDE_RUNNER) || getDeckAgenda() == getDeckAgendaMinimum() || getDeckAgenda() == (getDeckAgendaMinimum()+1));
	}
	
	public UUID getUUID() {
		return mUUID;
	}
	
	public Long getRowId() {
		return mRowId;
	}
	public void setRowId(Long newRowId) {
		mRowId = newRowId;
	}
	
	public void setCardsToAdd(ArrayList<CardCount> list) {
		mCardsToAdd.clear();
		for (CardCount cc : list) {
			mCardsToAdd.put(cc.getCard(), cc);
		}
	}
	
	public void setCardsToRemove(ArrayList<CardCount> list) {
		mCardsToRemove.clear();
		for (CardCount cc : list) {
			mCardsToRemove.put(cc.getCard(), cc);
		}
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		JSONArray jsonCards = new JSONArray();
		JSONArray jsonCardsToAdd = new JSONArray();
		JSONArray jsonCardsToRemove = new JSONArray();
		try {
			// Deck info
			json.putOpt(JSON_DECK_UUID, mUUID.toString());
			json.putOpt(JSON_DECK_NAME, mName);
			json.putOpt(JSON_DECK_NOTES, mNotes);
			json.putOpt(JSON_DECK_IDENTITY_CODE, mIdentity.getCode());
			
			// Cards
			ArrayList<Card> cardList = getCards();
			for (Card card : cardList) {
				JSONObject jsonCard = new JSONObject();
				jsonCard.putOpt(JSON_DECK_CARD_CODE, card.getCode());
				jsonCard.putOpt(JSON_DECK_CARD_COUNT, getCardCount(card));
				jsonCards.put(jsonCard);
			}
			
			// Cards to add
			for (CardCount cardCount : getCardsToAdd()) {
				JSONObject jsonCardCount = new JSONObject();
				jsonCardCount.putOpt(JSON_DECK_CARD_CODE, cardCount.getCard().getCode());
				jsonCardCount.putOpt(JSON_DECK_CARD_COUNT, cardCount.getCount());
				jsonCardCount.putOpt(JSON_DECK_CARDS_DONE, cardCount.isDone());
				jsonCardsToAdd.put(jsonCardCount);
			}
			
			// Cards to remove
			for (CardCount cardCount : getCardsToRemove()) {
				JSONObject jsonCardCount = new JSONObject();
				jsonCardCount.putOpt(JSON_DECK_CARD_CODE, cardCount.getCard().getCode());
				jsonCardCount.putOpt(JSON_DECK_CARD_COUNT, cardCount.getCount());
				jsonCardCount.putOpt(JSON_DECK_CARDS_DONE, cardCount.isDone());
				jsonCardsToRemove.put(jsonCardCount);
			}
			
			json.putOpt(JSON_DECK_CARDS, jsonCards);
			json.putOpt(JSON_DECK_CARDS_TO_ADD, jsonCardsToAdd);
			json.putOpt(JSON_DECK_CARDS_TO_REMOVE, jsonCardsToRemove);
		} catch (JSONException e) {
			// 
			e.printStackTrace();
		}
		return json;
	}
	
	public static Deck fromJSON(JSONObject json) {
		Deck deck = new Deck();
		deck.mUUID = UUID.fromString(json.optString(JSON_DECK_UUID, UUID.randomUUID().toString()));
		deck.setName(json.optString(JSON_DECK_NAME));
		deck.setIdentity(AppManager.getInstance().getAllCards().getCard(json.optString(JSON_DECK_IDENTITY_CODE)));
		deck.setNotes(json.optString(JSON_DECK_NOTES));
		
		// Get the cards
		try {
			JSONArray jsonCards = json.getJSONArray(JSON_DECK_CARDS);
			for (int i = 0; i < jsonCards.length(); i++) {
				JSONObject jsonCard = jsonCards.getJSONObject(i);
				deck.setCardCount(AppManager.getInstance().getCard(jsonCard.optString(JSON_DECK_CARD_CODE)), jsonCard.optInt(JSON_DECK_CARD_COUNT));
			}
			
			// By default, when a new card is added to a deck, it is added to the ADD list
			deck.mCardsToAdd.clear();
			deck.mCardsToRemove.clear();
		} catch (JSONException e) { }
		
		// Get the cards to add
		try {
			JSONArray jsonCards = json.getJSONArray(JSON_DECK_CARDS_TO_ADD);
			for (int i = 0; i < jsonCards.length(); i++) {
				JSONObject jsonCard = jsonCards.getJSONObject(i);
				Card card = AppManager.getInstance().getCard(jsonCard.optString(JSON_DECK_CARD_CODE));
				deck.mCardsToAdd.put(card, new CardCount(card, jsonCard.optInt(JSON_DECK_CARD_COUNT), jsonCard.optBoolean(JSON_DECK_CARDS_DONE)));
			}
		} catch (JSONException e) { }
		
		// Get the cards to remove
		try {
			JSONArray jsonCards = json.getJSONArray(JSON_DECK_CARDS_TO_REMOVE);
			for (int i = 0; i < jsonCards.length(); i++) {
				JSONObject jsonCard = jsonCards.getJSONObject(i);
				Card card = AppManager.getInstance().getCard(jsonCard.optString(JSON_DECK_CARD_CODE));
				deck.mCardsToRemove.put(card, new CardCount(card, jsonCard.optInt(JSON_DECK_CARD_COUNT), jsonCard.optBoolean(JSON_DECK_CARDS_DONE)));
			}
		} catch (JSONException e) { }
		
		return deck;
	}
	
	public Deck clone(Context context) {
		Deck newDeck = Deck.fromJSON(this.toJSON());
		DatabaseHelper db = new DatabaseHelper(context);
		newDeck.setName(String.format(context.getResources().getString(R.string.copy_of), newDeck.getName()));
		// Do not clone the cards to add and cards to remove
		newDeck.setCardsToAdd(new ArrayList<CardCount>());
		newDeck.setCardsToRemove(new ArrayList<CardCount>());
		
		// Save in the database
		db.createDeck(newDeck);
		db.saveDeck(newDeck);
		return newDeck;
	}

    public boolean isStarred() {
        return mStarred;
    }

    public void setStarred(boolean starred) {
        this.mStarred = starred;
    }
}
