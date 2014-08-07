package com.shuneault.netrunnerdeckbuilder.game;

import java.util.ArrayList;

public class CardList extends ArrayList<Card> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CardList mIdentities;
	private ArrayList<String> mFactions;
	private ArrayList<String> mSide;
	private ArrayList<String> mCardType;

	/**
	 * 
	 * @param faction: get specified faction, null to return all
	 * @param side: get specified side, null to return all
	 * @param type: get specified type, null to return all
	 * @return
	 */
	private CardList getCards(String factionCode, String sideCode, String typeCode) {
		CardList newList = new CardList();
		
		for (Card theCard : this) {
			if ( (factionCode == null || theCard.getFactionCode().equals(factionCode)) &&
					(sideCode == null || theCard.getSideCode().equals(sideCode)) &&
					(typeCode == null || theCard.getTypeCode().equals(typeCode))) {
				newList.add(theCard);
			}
		}
		
		return newList;
	}
	
	public CardList getIdentities() {
		// Caching
		if (mIdentities != null)
			return mIdentities;
		mIdentities = getCards(null, null, Card.Type.IDENTITY);
		return mIdentities;
	}
	
	public CardList getIdentities(String sideCode) {
		CardList newList = new CardList();
		// Caching
		if (mIdentities == null)
			mIdentities = getIdentities();
		
		for (Card theCard : mIdentities) {
			if (theCard.getSideCode().equals(sideCode)) {
				newList.add(theCard);
			}
		}
		
		return newList;
	}
	
	public ArrayList<String> getFactions() {
		// Caching
		if (mFactions != null)
			return mFactions;
		mFactions = new ArrayList<String>();
		for (Card theCard : this) {
			if (!mFactions.contains(theCard.getFaction()))
				mFactions.add(theCard.getFaction());
		}
		return mFactions;
	}
	
	public ArrayList<String> getSide() {
		// Caching
		if (mSide != null)
			return mSide;
		mSide = new ArrayList<String>();
		for (Card theCard : this) {
			if (!mSide.contains(theCard.getSide()))
				mSide.add(theCard.getSide());
		}
		return mSide;
	}
	
	public ArrayList<String> getCardType() {
		// Caching
		if (mCardType != null)
			return mCardType;
		mCardType = new ArrayList<String>();
		for (Card theCard : this) {
			if (!mCardType.contains(theCard.getType()))
				mCardType.add(theCard.getType());
		}
		return mCardType;
	}
	
	public ArrayList<String> getCardType(String side) {
		mCardType = new ArrayList<String>();
		for (Card theCard : this) {
			if (theCard.getSide().equals(side) && !mCardType.contains(theCard.getType()))
				mCardType.add(theCard.getType());
		}
		return mCardType;
	}
	
	public Card getCard(String cardCode) {
		for (Card theCard : this)
			if (theCard.getCode().equals(cardCode))
				return theCard;
		
		return null;
	}
	

	
}
