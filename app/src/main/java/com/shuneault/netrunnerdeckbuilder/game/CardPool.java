package com.shuneault.netrunnerdeckbuilder.game;

import java.util.ArrayList;

public class CardPool {
    private static final int DEFAULT_QUANTITY = 3;
    private final int iAmountCoreDecks;

    private ArrayList<String> packFilter;

    public CardPool(ArrayList<String> packFilter, int coreCount) {
        this.packFilter = packFilter;
        iAmountCoreDecks = coreCount;
    }

    public int getMaxCardCount(Card card) {
        try {
            int count;
            count = getQuantityAvailable(card);

            count = Math.min(count, card.getDeckLimit());
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return DEFAULT_QUANTITY;
        }
    }

    private int getQuantityAvailable(Card card) {
        //todo: make this pack-aware
        int count;
        if (this.isInCoreSet(card)) {
            count =  Math.min(iAmountCoreDecks * card.getQuantity(), DEFAULT_QUANTITY);
        } else {
            count =  card.getQuantity();
        }
        return count;
    }

    private boolean isInCoreSet(Card card) {
        return card.getSetCode().equals(Card.SetName.CORE_SET)
                || card.getSetCode().equals(Card.SetName.REVISED_CORE_SET);
    }

    public ArrayList<String> getPackFilter() {
        return packFilter;
    }

    public boolean isFiltered() {
        return packFilter.size() > 0;
    }
}
