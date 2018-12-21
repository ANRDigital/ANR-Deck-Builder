package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;

import java.util.ArrayList;

public class DeckRepository implements IDeckRepository {

    private final DatabaseHelper dbHelper;
    private final CardRepository cardRepo;
    private final ArrayList<Deck> mDecks;

    public DeckRepository(CardRepository cardRepo, DatabaseHelper dbHelper){
        this.cardRepo = cardRepo;
        this.dbHelper = dbHelper;
        mDecks = dbHelper.getAllDecks(true, this.cardRepo);
    }

    @Override
    public Deck getDeck(long deckId) {
        for (Deck deck : this.mDecks) {
            if (deck.getRowId() == deckId) {
                return deck;
            }
        }
        return null;
    }

    @Override
    public ArrayList<Deck> getAllDecks() {
        return mDecks;
    }

    @Override
    public void addDeck(Deck deck) {
        mDecks.add(deck);
    }

    @Override
    public boolean deleteDeck(Deck deck) {
        dbHelper.deleteDeck(deck);
        return mDecks.remove(deck);
    }

    @Override
    public void changeIdentity(Deck deck, String identityCode) {
        Card identity = cardRepo.getCard(identityCode);
        Deck d = getDeck(deck.getRowId());
        d.setIdentity(identity);
        dbHelper.updateDeck(d);
    }

    @Override
    public Long cloneDeck(Deck deck) {
        Deck newDeck = Deck.fromJSON(deck.toJSON(), cardRepo);
        newDeck.setName(String.format("Copy of %1$s", newDeck.getName()));
        // Do not clone the cards to add and cards to remove
        newDeck.setCardsToAdd(new ArrayList<>());
        newDeck.setCardsToRemove(new ArrayList<>());

        // Save in the database
        doCreateDeck(newDeck);
        return newDeck.getRowId();
    }

    private void doCreateDeck(Deck newDeck) {
        dbHelper.saveDeck(newDeck);
        addDeck(newDeck);
    }

    @Override
    public void createDeck(Deck deck) {
        doCreateDeck(deck);
    }

    @Override
    public void setDeckFormat(Deck deck, Format format) {
        deck.setFormat(format);
        dbHelper.updateDeck(deck);
    }

    @Override
    public void saveDeck(Deck deck) {
        dbHelper.saveDeck(deck);
    }

    @Override
    public void updateDeck(Deck deck) {
        dbHelper.updateDeck(deck);
    }
}
