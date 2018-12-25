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
     * @param factionCode: get specified faction, null to return all
     * @param sideCode:    get specified side, null to return all
     * @param typeCode:    get specified type, null to return all
     * @return CardList containing relevant cards
     */
    private CardList getCards(String factionCode, String sideCode, String typeCode) {
        CardList newList = new CardList();

        for (Card theCard : this) {
            if ((factionCode == null || theCard.getFactionCode().equals(factionCode)) &&
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
            if (!mFactions.contains(theCard.getFactionCode()))
                mFactions.add(theCard.getFactionCode());
        }
        return mFactions;
    }

    public ArrayList<String> getSide() {
        // Caching
        if (mSide != null)
            return mSide;
        mSide = new ArrayList<String>();
        for (Card theCard : this) {
            if (!mSide.contains(theCard.getSideCode()))
                mSide.add(theCard.getSideCode());
        }
        return mSide;
    }

    public ArrayList<String> getCardType() {
        // Caching
        if (mCardType != null)
            return mCardType;
        mCardType = new ArrayList<String>();
        for (Card theCard : this) {
            if (!mCardType.contains(theCard.getTypeCode()))
                mCardType.add(theCard.getTypeCode());
        }
        return mCardType;
    }

    public ArrayList<String> getCardType(String side) {
        mCardType = new ArrayList<String>();
        for (Card theCard : this) {
            if (theCard.getSideCode().equals(side) && !mCardType.contains(theCard.getTypeCode()))
                mCardType.add(theCard.getTypeCode());
        }
        return mCardType;
    }

    public Card getCard(String cardCode) {
        for (Card theCard : this)
            if (theCard.getCode().equals(cardCode))
                return theCard;

        Card card = new Card(cardCode);
        card.setTitle("unknown card: " + cardCode);
        card.setIsUnknown();
        
        return card;
    }


    public CardList getPackCards(ArrayList<Pack> packList) {
        CardList cd = new CardList();

        for (Pack p : packList) {
            // add cards pointing AT the pack
            for (Card card : this) {
                if (p.getCode().equals(card.getCode())) {
                    cd.add(card);
                }
            }

            // add cards linked FROM the pack
            for (CardLink link: p.getCardLinks()){
                Card c = getCard(link.getCardCode());
                if(!cd.contains(c)){
                    cd.add(c);
                }
            }
        }
        return cd;
    }

    public void addExtras(ArrayList<Card> cards) {
        this.removeAll(cards);
        this.addAll(cards);
    }
}
