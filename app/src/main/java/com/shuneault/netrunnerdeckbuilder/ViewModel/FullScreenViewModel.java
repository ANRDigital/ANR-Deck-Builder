package com.shuneault.netrunnerdeckbuilder.ViewModel;

import androidx.lifecycle.ViewModel;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;

import java.util.ArrayList;
import java.util.Collections;

public class FullScreenViewModel extends ViewModel {

    private String setName;
    private String cardCode;
    private int position;
    private ArrayList<String> cardCodes;
    private IDeckRepository deckRepository;
    private Deck mDeck;
    private CardRepository cardRepository;
    private ArrayList<Card> mCards = new ArrayList<>();

    public FullScreenViewModel(IDeckRepository deckRepository, CardRepository cardRepository) {
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public String getSetName() {
        return setName;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
        mCards.add(cardRepository.getCard(cardCode));
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setCardCodes(ArrayList<String> cardCodes) {
        this.cardCodes = cardCodes;
        mCards.clear();
        mCards.addAll(cardRepository.getCards(cardCodes));
        // NO Card Sort
    }

    public ArrayList<String> getCardCodes() {
        return cardCodes;
    }

    public void loadDeck(long deckId) {
        mDeck = this.deckRepository.getDeck(deckId);
        mCards = mDeck.getCards();
        Collections.sort(mCards, new Sorter.CardSorterByCardType());
    }

    public Deck getDeck() {
        return mDeck;
    }

    public ArrayList<Card> getCards() {
        return mCards;
    }

    public String getFactionCode() {
        if (mDeck != null) {
            return mDeck.getIdentity().getFactionCode();
        }
        if (cardCode != null) {
            return mCards.get(0).getFactionCode();
        }
        return null;
    }
}
