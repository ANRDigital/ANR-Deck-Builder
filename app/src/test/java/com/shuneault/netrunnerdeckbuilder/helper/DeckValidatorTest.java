package com.shuneault.netrunnerdeckbuilder.helper;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardBuilder;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.game.FormatBuilder;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.game.Pack;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class DeckValidatorTest {
    private ArrayList<Pack> packs;
    private String legalSetCode = "sc19";

    @Before
    public void Setup(){
        packs = new ArrayList<>();

        Pack p = new Pack();
        p.setCode(legalSetCode);
        packs.add(p);
    }

    @Test
    public void NoMWLNoRotation_OnlylegalCards_PassesValidation(){
        MostWantedList mwl = new MostWantedList();
        DeckValidator validator = new DeckValidator(mwl);
        Card idCard = new CardBuilder("").withCode("idCard").Build();
        Format format = new FormatBuilder().asCoreExperience().Build();
        Deck deck = new Deck(idCard, format);
        Card legalCard = new CardBuilder("").withSetCode(legalSetCode).Build();
        assertTrue(format.getPacks().contains(legalSetCode));
        deck.updateCardCount(legalCard, 2);
        assertEquals(1, deck.getCards().size());

        boolean valid = validator.validate(deck, packs);
        assertTrue(valid);
    }

    @Test
    public void NoMWLNoRotation_IllegalCards_FailValidation(){
        MostWantedList mwl = new MostWantedList();
        DeckValidator validator = new DeckValidator(mwl);
        Card idCard = new CardBuilder("").withCode("idCard").Build();
        Format format = new FormatBuilder().asCoreExperience().Build();
        Deck deck = new Deck(idCard, format);
        String illegalSetCode = "illegal-notSC19";
        Card illegalCard = new CardBuilder("").withSetCode(illegalSetCode).Build();
        assertFalse(format.getPacks().contains(illegalSetCode));
        deck.updateCardCount(illegalCard,2);
        assertEquals(1, deck.getCards().size());

        boolean valid = validator.validate(deck, packs);
        assertFalse(valid);

    }

}