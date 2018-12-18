package com.shuneault.netrunnerdeckbuilder.helper;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardMWL;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.game.Pack;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class DeckValidator {
    private final MostWantedList mMostWantedList;

    public DeckValidator(MostWantedList mwl) {
        mMostWantedList = mwl;
    }

    public boolean validate(Deck deck, ArrayList<Pack> packs) {
        // if there's no mwl set up then pass
        boolean mwlValid = mMostWantedList == null || CheckMwl(deck);
        boolean cardPoolValid = CheckCardPool(deck, packs);
        return mwlValid && cardPoolValid;
    }

    private boolean CheckCardPool(Deck deck, ArrayList<Pack> packs) {
        ArrayList<String> packCodes = new ArrayList<>();
        for (Pack p : packs) {
            packCodes.add(p.getCode());
        }
        if (!packCodes.contains(deck.getIdentity().getSetCode()))
            return false;

        for (Card c : deck.getCards()) {
            if (!packCodes.contains(c.getSetCode())){
                return false;
            }
        }
        return true;
    }

    private boolean CheckMwl(Deck deck) {
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