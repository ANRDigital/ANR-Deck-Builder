package com.shuneault.netrunnerdeckbuilder.ViewModel;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.CardPool;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class BrowseCardsViewModel extends ViewModel {
    private CardRepository cardRepo;
    private CardPool mPool;
    private CardList mCards;

    public BrowseCardsViewModel(CardRepository cardRepo) {
        this.cardRepo = cardRepo;
    }

    public void init() {
        mPool = cardRepo.getGlobalCardPool();
        mCards = mPool.getCards();

    }

    public CardList getCards() {
        return mCards;
    }

    public void doSearch(String searchText) {
        mCards.clear();
        mCards.addAll(cardRepo.searchCards(searchText, mPool));
    }

    public void updatePackFilter(ArrayList<String> packFilter) {
        if (packFilter.isEmpty())
            mPool = cardRepo.getGlobalCardPool();
        else
            mPool = cardRepo.getCardPool(packFilter);
    }
}
