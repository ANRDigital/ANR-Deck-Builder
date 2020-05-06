package com.shuneault.netrunnerdeckbuilder.game;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CardListTest {

    @Test
    public void OnAddExtras_WhenACardIsCommon_NoDuplicatesResult() {
        CardList listOne = new CardList();

        Card c = new CardBuilder().withCode("1234").Build();
        listOne.add(c);
        CardList listTwo = new CardList();
        Card c2 = new CardBuilder().withCode("2345").Build();
        listTwo.add(c);
        listTwo.add(c2);

        listOne.addExtras(listTwo);

        assertEquals(2, listOne.size());
    }

    @Test
    public void OnAddExtras_WhenACardIsNotCommon_CountIsSumOfBoth() {
        CardList listOne = new CardList();

        Card c = new CardBuilder().withCode("1234").Build();
        listOne.add(c);
        CardList listTwo = new CardList();
        Card c2 = new CardBuilder().withCode("2345").Build();
        Card c3 = new CardBuilder().withCode("3456").Build();
        listTwo.add(c3);
        listTwo.add(c2);

        listOne.addExtras(listTwo);

        assertEquals(3, listOne.size());
    }


}