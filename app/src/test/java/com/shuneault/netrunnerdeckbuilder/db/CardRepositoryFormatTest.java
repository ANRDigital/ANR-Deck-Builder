package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardBuilder;
import com.shuneault.netrunnerdeckbuilder.game.CardPool;
import com.shuneault.netrunnerdeckbuilder.game.Cycle;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.game.FormatBuilder;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.game.Rotation;
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CardRepositoryFormatTest {
    private JSONDataLoader mLoaderMock;
    private ArrayList<Pack> mPacks;
    private ArrayList<Card> mCards;
    private ArrayList<Cycle> mCycles;
    private ArrayList<Rotation> mRotations;

    @Before
    public void Setup() throws IOException, JSONException {
        mLoaderMock = mock(JSONDataLoader.class);
        MwlData mwlDetails = mock(MwlData.class);
        when(mLoaderMock.getMwlDetails()).thenReturn(mwlDetails);

        mRotations = new ArrayList<Rotation>();
        Rotation r = new Rotation();
        r.setCode("rotation-2018");
        r.getCycles().add("sc19");
        r.getCycles().add("c2");
        mRotations.add(r);
        when(mLoaderMock.getRotations()).thenReturn(mRotations);

        mCycles = new ArrayList<>();
        mCycles.add(new CycleBuilder().withCode("sc19").Build());
        mCycles.add(new CycleBuilder().withCode("c1").withRotation(true).Build());
        mCycles.add(new CycleBuilder().withCode("c2").withRotation(false).Build());
        when(mLoaderMock.getCyclesFromFile()).thenReturn(mCycles);

        mPacks = new ArrayList<>();
        mPacks.add(new PackBuilder().withCode("sc19").withName("Core Experience").withCycle("sc19").Build());
        mPacks.add(new PackBuilder().withCode("2345").withName("Pack Two").withCycle("c1").Build());
        mPacks.add(new PackBuilder().withCode("3456").withName("Pack 3").withCycle("c2").Build());

        when(mLoaderMock.getPacksFromFile()).thenReturn(mPacks);

        mCards = new ArrayList<>();
        // core
        mCards.add(new CardBuilder("").withCode("123").withSetCode("sc19").withQuantity(2).Build());
        mCards.add(new CardBuilder("").withCode("234").withSetCode("sc19").Build());
        mCards.add(new CardBuilder("").withCode("345").withSetCode("sc19").Build());

        // rotated
        mCards.add(new CardBuilder("").withCode("987").withSetCode("2345").Build());
        mCards.add(new CardBuilder("").withCode("876").withSetCode("2345").Build());
        mCards.add(new CardBuilder("").withCode("765").withSetCode("2345").Build());

        // not rotated
        mCards.add(new CardBuilder("").withCode("666").withSetCode("3456").Build());
        mCards.add(new CardBuilder("").withCode("777").withSetCode("3456").Build());
        mCards.add(new CardBuilder("").withCode("888").withSetCode("3456").Build());

        when(mLoaderMock.getCardsFromFile(anyString())).thenReturn(mCards);
    }

    @Test
    public void GetCardPool_FromFormat_CoreExperience() {

        ArrayList<String> packfilter = new ArrayList<>();
        packfilter.add("Pack One");
        CardRepository.CardRepositoryPreferences prefs = new CardRepository.CardRepositoryPreferences(1,
                packfilter);

        ISettingsProvider settingsProvider = mock(ISettingsProvider.class);
        when(settingsProvider.getCardRepositoryPreferences()).thenReturn(prefs);
        when(settingsProvider.getLanguagePref()).thenReturn("en");

        CardRepository repo = new CardRepository(null, settingsProvider, mLoaderMock);

        Format format = new FormatBuilder().asCoreExperience().Build();
        CardPool pool = repo.getCardPool(format, new ArrayList<>(), 0);

        assertNotNull(pool);

        assertEquals(3, pool.getCards().size());
        assertEquals(2, pool.getMaxCardCount(pool.getCards().get(0)));
    }

    @Test
    public void GetCardPool_FromFormat_Standard() {

        ArrayList<String> packfilter = new ArrayList<>();
        packfilter.add("Pack One");
        CardRepository.CardRepositoryPreferences prefs = new CardRepository.CardRepositoryPreferences(1,
                packfilter);

        ISettingsProvider settingsProvider = mock(ISettingsProvider.class);
        when(settingsProvider.getCardRepositoryPreferences()).thenReturn(prefs);
        when(settingsProvider.getLanguagePref()).thenReturn("en");

        CardRepository repo = new CardRepository(null, settingsProvider, mLoaderMock);

        Format format = new FormatBuilder().asStandard().Build();
        CardPool pool = repo.getCardPool(format, new ArrayList<>(), 0);

        assertNotNull(pool);
        assertEquals(6, pool.getCards().size());
        Card card = pool.getCards().get(0);
        assertEquals("123", card.getCode());
        assertEquals(3, pool.getMaxCardCount(card));
    }
}