package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardBuilder;
import com.shuneault.netrunnerdeckbuilder.game.CardPool;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
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

public class CardRepositoryTest {
    private JSONDataLoader mLoaderMock;
    private ArrayList<Pack> mPacks;
    private ArrayList<Card> mCards;

    @Before
    public void Setup() throws IOException, JSONException {
        mLoaderMock = mock(JSONDataLoader.class);
        MwlData mwlDetails = mock(MwlData.class);
        when(mLoaderMock.getMwlDetails()).thenReturn(mwlDetails);
        mPacks = new ArrayList<>();
        mPacks.add(new PackBuilder().withCode("1234").withName("Pack One").Build());
        mPacks.add(new PackBuilder().withCode("2345").withName("Pack Two").Build());
        mPacks.add(new PackBuilder().withCode("3456").withName("Pack 3").Build());

        when(mLoaderMock.getPacksFromFile()).thenReturn(mPacks);

        mCards = new ArrayList<>();
        mCards.add(new CardBuilder("").withCode("123").withSetCode("1234").Build());
        mCards.add(new CardBuilder("").withCode("234").withSetCode("1234").Build());
        mCards.add(new CardBuilder("").withCode("345").withSetCode("1234").Build());

        mCards.add(new CardBuilder("").withCode("987").withSetCode("2345").Build());
        mCards.add(new CardBuilder("").withCode("876").withSetCode("2345").Build());
        mCards.add(new CardBuilder("").withCode("765").withSetCode("2345").Build());

        mCards.add(new CardBuilder("").withCode("666").withSetCode("3456").Build());
        mCards.add(new CardBuilder("").withCode("777").withSetCode("3456").Build());
        mCards.add(new CardBuilder("").withCode("888").withSetCode("3456").Build());

        when(mLoaderMock.getCardsFromFile(anyString(), any())).thenReturn(mCards);
    }

    @Test
    public void WhenGlobalPrefIsUseAllPacks_CardPoolFilterIsEmpty_despitepackpref() {

        ArrayList<String> packfilter = new ArrayList<>();
        packfilter.add("Pack One");
        CardRepository.CardRepositoryPreferences prefs = new CardRepository.CardRepositoryPreferences(1,
                true,
                packfilter);

        ISettingsProvider settingsProvider = mock(ISettingsProvider.class);
        when(settingsProvider.getCardRepositoryPreferences()).thenReturn(prefs);
        when(settingsProvider.getLanguagePref()).thenReturn("en");
        CardRepository repo = new CardRepository(null, settingsProvider, mLoaderMock);
        CardPool pool = repo.getGlobalCardPool();

        assertNotNull(pool);
        assertEquals(mCards.size(), pool.getCards().size());
    }

    @Test
    public void WhenGlobalPackFilterIsSet_CardSubsetReturned() {

        ArrayList<String> packfilter = new ArrayList<>();
        packfilter.add("Pack One");
        CardRepository.CardRepositoryPreferences prefs = new CardRepository.CardRepositoryPreferences(1,
                false,
                packfilter);

        ISettingsProvider settingsProvider = mock(ISettingsProvider.class);
        when(settingsProvider.getCardRepositoryPreferences()).thenReturn(prefs);
        when(settingsProvider.getLanguagePref()).thenReturn("en");
        CardRepository repo = new CardRepository(null, settingsProvider, mLoaderMock);
        CardPool pool = repo.getGlobalCardPool();

        assertNotNull(pool);
        assertEquals(3, pool.getCards().size());
    }
}