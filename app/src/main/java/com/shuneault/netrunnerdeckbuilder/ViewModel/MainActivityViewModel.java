package com.shuneault.netrunnerdeckbuilder.ViewModel;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {
    private CardRepository cardRepo;
    private IDeckRepository deckRepo;
    private ArrayList<Deck> decks;

    public MainActivityViewModel(CardRepository cardRepo, IDeckRepository deckRepo){
        this.cardRepo = cardRepo;
        this.deckRepo = deckRepo;
    }

    public Deck createDeck(String identityCardCode) {
        Card identity = cardRepo.getCard(identityCardCode);
        Format format = cardRepo.getDefaultFormat();
        Deck deck = new Deck(identity, format);
        deckRepo.createDeck(deck);

        return deck;
    }

    public ArrayList<Deck> getDecks() {
        if (decks == null)
        {
            decks = deckRepo.getAllDecks();
        }
        return decks;
    }

    public void starDeck(Deck deck, boolean isStarred) {
        deck.setStarred(isStarred);
        deckRepo.saveDeck(deck);
    }
}
