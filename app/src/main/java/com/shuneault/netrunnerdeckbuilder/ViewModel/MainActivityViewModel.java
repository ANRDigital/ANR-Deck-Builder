package com.shuneault.netrunnerdeckbuilder.ViewModel;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

public class MainActivityViewModel extends ViewModel {
    private CardRepository cardRepo;
    private IDeckRepository deckRepo;
    private MutableLiveData<ArrayList<Deck>> mCurrentDecks = new MutableLiveData<>(); // this needs renaming or splitting into runner / corp??

    public MainActivityViewModel(CardRepository cardRepo, IDeckRepository deckRepo){
        this.cardRepo = cardRepo;
        this.deckRepo = deckRepo;
    }

    // Only the selected tab decks
    public MutableLiveData<ArrayList<Deck>> getDecksForSide(String side){
        ArrayList<Deck> decks = new ArrayList<>();

        // Only the selected tab decks
        for (Deck deck: deckRepo.getAllDecks()) {
            if (deck != null && deck.getSide().equals(side)) {
                decks.add(deck);
            }
        }
        // Sort the list
        Collections.sort(decks, new Sorter.DeckSorter());
        mCurrentDecks.setValue(decks);

        return mCurrentDecks;
    }

    public Deck createDeck(String identityCardCode) {
        Card identity = cardRepo.getCard(identityCardCode);
        Format format = cardRepo.getDefaultFormat();
        Deck deck = new Deck(identity, format);
        deckRepo.createDeck(deck);

        return deck;
    }

    public void starDeck(Deck deck, boolean isStarred) {
        deck.setStarred(isStarred);
        deckRepo.saveDeck(deck);
        //todo: sort decks
//        Collections.sort(decks, new Sorter.DeckSorter());
    }

    public void cloneDeck(@NotNull Deck deck) {
        deckRepo.cloneDeck(deck);
    }
}
