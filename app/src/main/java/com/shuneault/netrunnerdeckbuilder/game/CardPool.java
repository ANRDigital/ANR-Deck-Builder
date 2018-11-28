package com.shuneault.netrunnerdeckbuilder.game;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;

import java.util.ArrayList;

public class CardPool {
    private static final int DEFAULT_CARD_LIMIT = 3;
    private final CardCountList mCardLimits;

    private ArrayList<String> packFilter = new ArrayList<>();

    public CardPool(int coreCount, CardRepository repo, ArrayList<Pack> packFilter) {
        for (Pack p :
                packFilter) {
            this.packFilter.add(p.getName());
        }

        mCardLimits = new CardCountList();
        // loop the packFilter
        for (Pack p :
                packFilter) {
            // is it a core set?
            int setCount = p.isCoreSet() ? coreCount : 1;

            // are there card links?
            ArrayList<CardLink> cardLinks = p.getCardLinks();
            if (cardLinks.size() > 0){
                // if so loop the links
                for (CardLink cl :
                        cardLinks) {
                    // get the card and quantity to add
                    Card card = repo.getCard(cl.getCardCode());
                    int count = cl.getQuantity();
                    addCard(card, count * setCount);
                }
            }
            else
            {
                CardList packCards = repo.getPackCards(p);
                for (Card c :
                        packCards) {
                    addCard(c, c.getQuantity() * setCount);
                }
            }
        }
    }

    private void addCard(Card card, int count) {
        // are there existing cards in the pool?
        CardCount cc = mCardLimits.getCardCount(card);
        if(cc != null){
            // add the numbers together
            count += cc.getCount();
        }

        // ensure deck limits are observed - if not set use default limit
        int deckLimit = card.getDeckLimit() > 0 ? card.getDeckLimit() : DEFAULT_CARD_LIMIT;
        count = Math.min(count, deckLimit);

        mCardLimits.setCount(card, count);
    }

    public int getMaxCardCount(Card card) {
        CardCount cardCount = mCardLimits.getCardCount(card);

        return cardCount != null ? cardCount.getCount() : 0 ;
    }

    public ArrayList<String> getPackFilter() {
        return packFilter;
    }

    public boolean isFiltered() {
        return packFilter.size() > 0;
    }

    public CardList getCards() {
        CardList cards = new CardList();
        for (CardCount cc :
                mCardLimits) {
            cards.add(cc.getCard());
        }
        return cards;
    }

    public ArrayList<Card> getIdentities(String sideCode) {
        return getCards().getIdentities(sideCode);
    }
}
