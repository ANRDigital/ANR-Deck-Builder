package com.shuneault.netrunnerdeckbuilder.game;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CardPoolTest {
    @Test
    public void testBasic(){
        CardRepository repoMock = mock(CardRepository.class);
        Card card = new Card();
        card.setCode("12345");
        when(repoMock.getCard(anyString())).thenReturn(card);
        CardList cards = new CardList();
        cards.add(card);
        when(repoMock.getPackCards(any(Pack.class))).thenReturn(cards);
        ArrayList<Pack> emptyPackFilter = new ArrayList<>();
        CardPool pool = new CardPool(1, repoMock, emptyPackFilter);

        Assert.assertNotNull(pool);

    }

}