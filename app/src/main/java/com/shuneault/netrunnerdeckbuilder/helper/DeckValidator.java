package com.shuneault.netrunnerdeckbuilder.helper;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardMWL;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;

public class DeckValidator {
    private final MostWantedList mMostWantedList;

    public DeckValidator(MostWantedList mwl) {
        mMostWantedList = mwl;

    }

    public boolean Validate(Deck deck) {
        int restrictedCount = 0;
        boolean exceedsDeckLimits = false;

        // identity check for restricted
        CardMWL identityMwl = mMostWantedList.GetCardMWL(deck.getIdentity());
        if (identityMwl != null) {
            if (identityMwl.isRestricted()) {
                restrictedCount++;
            }

            if (identityMwl.hasDeckLimit() && deck.getIdentity().getQuantity() > identityMwl.getDeckLimit()) {
                exceedsDeckLimits = true;
            }
        }

        // check the rest of the deck
        for (Card card : deck.getCards()) {
            CardMWL cardMwl = mMostWantedList.GetCardMWL(card);
            if (cardMwl != null) {
                if (cardMwl.isRestricted()) {
                    restrictedCount++;
                }

                if (cardMwl.hasDeckLimit() && card.getQuantity() > cardMwl.getDeckLimit()) {
                    exceedsDeckLimits = true;
                }
            }
        }

        return restrictedCount < 2 && !exceedsDeckLimits;
    }
}