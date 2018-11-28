package com.shuneault.netrunnerdeckbuilder.game;

import java.util.ArrayList;

public class CardCountList extends ArrayList<CardCount> {
    public CardCount getCardCount(Card card) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getCard().getCode().equals(card.getCode())){
                return this.get(i);
            }
        }

        return null;
    }

    public void setCount(Card card, int count) {
        CardCount cardCount = this.getCardCount(card);
        if (cardCount != null){
            cardCount.setCount(count);
        }
        else
        {
            this.add(new CardCount(card, count));
        }
    }
}
