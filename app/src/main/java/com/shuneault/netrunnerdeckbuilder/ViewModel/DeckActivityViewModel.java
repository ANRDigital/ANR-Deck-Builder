package com.shuneault.netrunnerdeckbuilder.ViewModel;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.CardPool;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.helper.DeckValidator;
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import androidx.lifecycle.ViewModel;

public class DeckActivityViewModel extends ViewModel {
    private final ISettingsProvider settingsProvider;
    private Deck mDeck;
    private IDeckRepository mDeckRepo;
    private CardRepository mCardRepo;
    private boolean valid;
    private CardPool mCardPool;


    public DeckActivityViewModel(IDeckRepository mDeckRepo, CardRepository mCardRepo, ISettingsProvider settingsProvider) {
        this.mDeckRepo = mDeckRepo;
        this.mCardRepo = mCardRepo;
        this.settingsProvider = settingsProvider;
    }

    public void setDeckId(long deckId) {
        if(mDeck == null){
            reloadDeck(deckId);
        }
    }

    private void reloadDeck(long deckId) {
        mDeck = mDeckRepo.getDeck(deckId);
        refreshCardPool();

        validateDeck();
    }

    public Deck getDeck() {
        return mDeck;
    }

    public void changeDeckIdentity(Deck mDeck, String identityCode) {
        mDeckRepo.changeIdentity(mDeck, identityCode);
        reloadDeck(this.mDeck.getRowId());
    }

    public void deleteDeck(Deck deck) {
        mDeckRepo.deleteDeck(deck);
    }

    public Long cloneDeck(Deck deck) {
        return mDeckRepo.cloneDeck(deck);
    }

    public boolean changeDeckFormat(Format format) {
        // only change format if different to the existing one.
        if(!format.getName().equals(mDeck.getFormat().getName()))
        {
            mDeckRepo.setDeckFormat(mDeck, format);
            mDeck.getPackFilter().clear();
            refreshCardPool();
            autoReplaceCards();
            validateDeck();
            mDeckRepo.saveDeck(mDeck);
            return true; // format was changed
        }
        return false; // format was not changed
    }

    private void refreshCardPool() {
        mCardPool = mCardRepo.getCardPool(mDeck.getFormat(), mDeck.getPackFilter(), mDeck.getCoreCount());
    }

    //todo: this probably belongs somewhere else
    private void autoReplaceCards() {
        Card identity = mDeck.getIdentity();
        Card id = mCardPool.findCardByTitle(identity.getTitle());
        if (id != null){
            mDeck.setIdentity(id);
        }

        // loop the cards in the deck and replace with
        for (Card card : mDeck.getCards()) {
            if (mCardPool.getMaxCardCount(card) == 0){
                Card replacement = mCardPool.findCardByTitle(card.getTitle());
                if (replacement != null){
                    mDeck.replaceCard(card, replacement);
                }
            }
        }
    }

    public void validateDeck() {
        Format format = mDeck.getFormat();
        MostWantedList mostWantedList = mCardRepo.getMostWantedList(format.getMwlId());
        ArrayList<Pack> packs = mCardRepo.getPacks(format, mDeck.getPackFilter());

        DeckValidator validator = new DeckValidator(mostWantedList);
        this.valid = validator.validate(mDeck, packs);
    }

    public boolean isValid() {
        return valid;
    }

    public void setDeckName(String name) {
        mDeck.setName(name);
    }

    public void setDeckDescription(String description) {
        mDeck.setNotes(description);
    }

    public void setPackFilter(ArrayList<String> packFilter) {
        mDeck.setPackFilter(packFilter);
        mDeckRepo.updateDeck(mDeck);

        refreshCardPool();

        validateDeck();
    }

    public void setCoreCount(int count) {
        mDeck.setCoreCount(count);
        mDeckRepo.updateDeck(mDeck);

        refreshCardPool();

        validateDeck();
    }

    public ArrayList<String> getCardHeaders() {
        String sideCode = mDeck.getIdentity().getSideCode();;
        ArrayList<String> headers = mCardRepo.getCardTypes(sideCode, false);
        Collections.sort(headers);
        return headers;
    }

    public HashMap<String, ArrayList<Card>> getGroupedCards(Deck deck, ArrayList<String> headers) {
        HashMap<String, ArrayList<Card>> mListCards = new HashMap<>();
        String sideCode = deck.getIdentity().getSideCode();;

        CardList cardCollection = mCardPool.getCards();
        cardCollection.addExtras(deck.getCards());
        for (Card theCard : cardCollection) {
            // Only add the cards that are on my side
            boolean isSameSide = theCard.getSideCode().equals(sideCode);

            // Do not add the identities
            boolean isIdentity = theCard.isIdentity();

            // Only display agendas that belong to neutral or my faction
            String deckFaction = deck.getIdentity().getFactionCode();
            boolean isGoodAgenda = !theCard.isAgenda()
                    || theCard.getFactionCode().equals(deckFaction)
                    || theCard.isNeutral();

            // Cannot add Jinteki card for "Custom Biotics: Engineered for Success" Identity
            boolean isJintekiOK = !theCard.isJinteki() || !deck.getIdentity().getCode().equals(Card.SpecialCards.CARD_CUSTOM_BIOTICS_ENGINEERED_FOR_SUCCESS);

            // Ignore non-virtual resources if runner is Apex and setting is set
            boolean isNonVirtualOK = true;
            if (theCard.isResource() && !theCard.isVirtual()) {
                if (deck.isApex() && settingsProvider.getHideNonVirtualApex()){
                    isNonVirtualOK = false;
                }
            }

            if (isSameSide && !isIdentity && isGoodAgenda && isJintekiOK && isNonVirtualOK) {
                // add the type grouping if it doesn't exist
                if (mListCards.get(theCard.getTypeCode()) == null)
                    mListCards.put(theCard.getTypeCode(), new ArrayList<>());

                // add the card to the type group
                mListCards.get(theCard.getTypeCode()).add(theCard);
            }
        }

        // Sort the cards
        sortListCards(headers, mListCards, deck.getIdentity().getFactionCode());
        return mListCards;
    }

    public HashMap<String, ArrayList<Card>> getMyGroupedCards(ArrayList<String> headers, Deck deck) {
        // Generate a new card list to display and notify the adapter
        HashMap<String, ArrayList<Card>> cardList = new HashMap<>();
        for (String theHeader : headers) {
            cardList.put(theHeader, new ArrayList<Card>());
        }
        for (Card theCard : deck.getCards()) {
            // Only add the cards that are on my side
            // Do not add the identities
            String typeCode = theCard.getTypeCode();
            String sideCode = theCard.getSideCode();
            if (!typeCode.equals(Card.Type.IDENTITY) && sideCode.equals(deck.getIdentity().getSideCode())) {
                if (cardList.get(typeCode) == null)
                    cardList.put(typeCode, new ArrayList<Card>());
                cardList.get(typeCode).add(theCard);
            }
        }
        sortListCards(headers, cardList, deck.getIdentity().getFactionCode());
        return cardList;
    }

    private void sortListCards(ArrayList<String> headers, HashMap<String, ArrayList<Card>> listCards, String factionCode) {
        // Sort by faction,
        for (String strCat : headers) {
            ArrayList<Card> list = listCards.get(strCat);
            if (list != null) {
                Collections.sort(list, new Sorter.CardSorterByFactionWithMineFirst(factionCode));
            }
        }
    }

    public void addCard(Card card) {
        int max = mCardPool.getMaxCardCount(card);
        mDeck.AddCard(card, max);
    }

    public void reduceCard(Card card) {
        mDeck.ReduceCard(card);
    }

    public void save() {
        mDeckRepo.saveDeck(mDeck);
    }
}

