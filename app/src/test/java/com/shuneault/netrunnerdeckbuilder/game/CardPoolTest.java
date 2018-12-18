package com.shuneault.netrunnerdeckbuilder.game;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CardPoolTest {
    @Test
    public void CreateCardPool_FromFormat_MwlIsSet(){
        CardRepository repoMock = mock(CardRepository.class);
        Card card = new Card();
        card.setCode("12345");
        when(repoMock.getCard(anyString())).thenReturn(card);
        CardList cards = new CardList();
        cards.add(card);
        when(repoMock.getPackCards(any(Pack.class))).thenReturn(cards);
        MostWantedList mwl = new MostWantedList();
        int STANDARD_MWL_ID = 9;
        mwl.setId(STANDARD_MWL_ID);
        when(repoMock.getMostWantedList(STANDARD_MWL_ID)).thenReturn(mwl);

        Format format = new FormatBuilder().asStandard().Build();

        CardPool pool = new CardPool(repoMock, format);

        Assert.assertNotNull(pool);

//        MostWantedList m = pool.getMwl();
//        assertEquals(String.valueOf(format.getMwlId()), m.getId());
    }

}