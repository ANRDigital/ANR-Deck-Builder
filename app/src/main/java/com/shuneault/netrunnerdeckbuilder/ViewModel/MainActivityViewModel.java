package com.shuneault.netrunnerdeckbuilder.ViewModel;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;

import androidx.lifecycle.ViewModel;
import kotlin.Lazy;

import static org.koin.java.standalone.KoinJavaComponent.inject;

public class MainActivityViewModel extends ViewModel {
    private Lazy<CardRepository> cardRepo = inject(CardRepository.class);
    private Lazy<IDeckRepository> deckRepo = inject(IDeckRepository.class);

    public Deck createDeck(Card card) {
        CardRepository repo = cardRepo.getValue();
        Format format = repo.getDefaultFormat();
        Deck deck = new Deck(card, format);
        deckRepo.getValue().createDeck(deck);

        return deck;
    }
}
