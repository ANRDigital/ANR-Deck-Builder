package com.shuneault.netrunnerdeckbuilder.ViewModel;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.CardPool;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

public class BrowseCardsViewModel extends ViewModel {
    private CardRepository cardRepo;
    private CardPool mPool;
    public ArrayList<String> packFilter = new ArrayList<>();
    private CardList mCards;
    private ISettingsProvider settingsProvider;
    public String searchText = "";
    public Format browseFormat;

    public BrowseCardsViewModel(CardRepository cardRepo, ISettingsProvider settingsProvider) {
        this.cardRepo = cardRepo;
        browseFormat = cardRepo.getDefaultFormat();
        this.settingsProvider = settingsProvider;
    }

    public void init() {
        if (mPool == null){
            mPool = cardRepo.getCardPool();
            mCards = mPool.getCards();
        }
    }

    public CardList getCardList() {
        return mCards;
    }

    public void doSearch(String searchText) {
        mCards.clear();
        mCards.addAll(cardRepo.searchCards(searchText, mPool));
    }

    public void updatePackFilter(ArrayList<String> packFilter) {
        mPool = cardRepo.getCardPool(this.browseFormat, packFilter);
        this.packFilter = packFilter;
    }

    public void useMyCollectionAsFilter() {
        packFilter = settingsProvider.getMyCollection();
    }

    public void setFilter(@NotNull ArrayList<String> values) {
        packFilter = values;
    }
}
