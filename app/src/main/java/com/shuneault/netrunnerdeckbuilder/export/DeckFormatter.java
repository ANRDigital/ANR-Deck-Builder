package com.shuneault.netrunnerdeckbuilder.export;

import com.shuneault.netrunnerdeckbuilder.game.Deck;

public interface DeckFormatter {
    String fromDeck(Deck deck);
}
