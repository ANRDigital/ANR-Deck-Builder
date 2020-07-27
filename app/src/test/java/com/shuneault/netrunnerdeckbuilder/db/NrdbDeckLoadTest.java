package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardBuilder;
import com.shuneault.netrunnerdeckbuilder.game.Cycle;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.game.FormatBuilder;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.game.Rotation;
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider;
import com.shuneault.netrunnerdeckbuilder.helper.NrdbDeckFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NrdbDeckLoadTest {
    private JSONDataLoader mLoaderMock;
    private ArrayList<Pack> mPacks;
    private ArrayList<Card> mCards;
    private ArrayList<Cycle> mCycles;
    private ArrayList<Rotation> mRotations;

//    @Before
//    public void Setup() throws IOException, JSONException {
//        mLoaderMock = mock(JSONDataLoader.class);
//        MwlData mwlDetails = mock(MwlData.class);
//        when(mLoaderMock.getMwlDetails()).thenReturn(mwlDetails);
//
//        mRotations = new ArrayList<>();
//        Rotation r = new Rotation();
//        r.setCode("rotation-2018");
//        r.getCycles().add("sc19");
//        r.getCycles().add("c2");
//        mRotations.add(r);
//        when(mLoaderMock.getRotations()).thenReturn(mRotations);
//
//        mCycles = new ArrayList<>();
//        mCycles.add(new CycleBuilder().withCode("sc19").Build());
//        mCycles.add(new CycleBuilder().withCode("c1").withRotation(true).Build());
//        mCycles.add(new CycleBuilder().withCode("c2").withRotation(false).Build());
//        when(mLoaderMock.getCyclesFromFile()).thenReturn(mCycles);
//
//        mPacks = new ArrayList<>();
//        mPacks.add(new PackBuilder().withCode("sc19").withName("Core Experience").withCycle("sc19").Build());
//        mPacks.add(new PackBuilder().withCode("2345").withName("Pack Two").withCycle("c1").Build());
//        mPacks.add(new PackBuilder().withCode("3456").withName("Pack 3").withCycle("c2").Build());
//
//        when(mLoaderMock.getPacksFromFile()).thenReturn(mPacks);
//
//        ArrayList<Format> mFormats = new ArrayList<>();
//        mFormats.add(new FormatBuilder().asStandard().Build());
//        when(mLoaderMock.getFormats()).thenReturn(mFormats);
//
//        mCards = new ArrayList<>();
//        // core
//        mCards.add(new CardBuilder().withCode("13033").withSetCode("sc19").withQuantity(2).Build());
//        mCards.add(new CardBuilder().withCode("20090").withSetCode("sc19").Build());
//        mCards.add(new CardBuilder().withCode("20110").withSetCode("sc19").Build());
//
//        // rotated
//        mCards.add(new CardBuilder().withCode("20112").withSetCode("2345").withTitle("Mr Anarchist").withTypeCode(Card.Type.IDENTITY).Build());
//        mCards.add(new CardBuilder().withCode("20113").withSetCode("2345").Build());
//        mCards.add(new CardBuilder().withCode("20115").withSetCode("2345").Build());
//
//        // not rotated
//        mCards.add(new CardBuilder().withCode("20116").withSetCode("3456").Build());
//        mCards.add(new CardBuilder().withCode("20117").withSetCode("3456").Build());
//        mCards.add(new CardBuilder().withCode("20119").withSetCode("3456").Build());
//
//        when(mLoaderMock.getCardsFromFile(anyString())).thenReturn(mCards);
//    }
//
//    @Test
//    public void BasicDeckJsonLoad() throws JSONException {
//        String data = "{\n" +
//                "            \"id\": 52856,\n" +
//                "            \"date_creation\": \"2018-10-06T06:57:37+00:00\",\n" +
//                "            \"date_update\": \"2018-10-06T06:58:30+00:00\",\n" +
//                "            \"name\": \"Phat Sparks\",\n" +
//                "            \"description\": \"<p>My first attempt to make an annoying credit taxing NBN deck. Limited to deluxe card pool plus kitara, just add lots of advertisements and fun stuff</p>\\n\",\n" +
//                "            \"user_id\": 30150,\n" +
//                "            \"user_name\": \"danj3000\",\n" +
//                "            \"tournament_badge\": false,\n" +
//                "            \"cards\": {\n" +
//                "                \"13033\": 3,\n" +
//                "                \"20090\": 2,\n" +
//                "                \"20110\": 3,\n" +
//                "                \"20112\": 1,\n" +
//                "                \"20113\": 2,\n" +
//                "                \"20115\": 3,\n" +
//                "                \"20116\": 2,\n" +
//                "                \"20117\": 1,\n" +
//                "                \"20119\": 1,\n" +
//                "            },\n" +
//                "            \"mwl_code\": \"NAPD_MWL_2.1\"\n" +
//                "        }";
//
//        JSONObject json = new JSONObject(data);
//
//        CardRepository.CardRepositoryPreferences prefs = new CardRepository.CardRepositoryPreferences(1);
//        Format standardFormat = new FormatBuilder().asStandard().Build();
//
//        ISettingsProvider settingsProvider = mock(ISettingsProvider.class);
//        when(settingsProvider.getCardRepositoryPreferences()).thenReturn(prefs);
//        when(settingsProvider.getLanguagePref()).thenReturn("en");
//        when(settingsProvider.getDefaultFormatId()).thenReturn(standardFormat.getId());
//
//        CardRepository repo = new CardRepository(null, settingsProvider, mLoaderMock);
//
//        NrdbDeckFactory factory = new NrdbDeckFactory(repo);
//        Deck deck = factory.createDeckFromJson(json);
//
//        assertNotNull(deck);
//        assertNotNull(deck.getIdentity());
//        assertEquals("Phat Sparks", deck.getName());
//        assertEquals(52856, deck.getNrdbId());
//        assertNotEquals("", deck.getNotes());
//        assertEquals(8, deck.getCards().size());
//    }

}