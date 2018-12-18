package com.shuneault.netrunnerdeckbuilder.game;

import org.junit.Test;

import static org.junit.Assert.*;

public class DeckTest {
    @Test
    public void EachDeck_MustBeInAChosenFormat(){
        Card identity = new Card();
        int expectedFormatId = 1;
        Format format = new FormatBuilder().withId(expectedFormatId).Build();
        Deck deck = new Deck(identity, format);

        Format f = deck.getFormat();

        assertNotNull(f);
        assertEquals(expectedFormatId, f.getId());
    }

}