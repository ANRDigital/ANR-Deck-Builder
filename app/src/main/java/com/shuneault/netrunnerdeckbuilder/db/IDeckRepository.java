package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;

import java.util.ArrayList;

public interface IDeckRepository {
    Deck getDeck(long deckId);

    ArrayList<Deck> getAllDecks();

    void addDeck(Deck deck);

    boolean deleteDeck(Deck deck);

    void changeIdentity(Deck mDeck, String identityCode);

    Long cloneDeck(Deck deck);

    void createDeck(Deck deck);

    void setDeckFormat(Deck mDeck, Format format);

    void saveDeck(Deck deck);
}
