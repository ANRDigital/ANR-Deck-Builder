package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.CardPool;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider;
import com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CardRepositoryTest {

    @Test
    public void testGetGlobalCardPool() throws IOException, JSONException {
        CardRepository.CardRepositoryPreferences prefs = new CardRepository.CardRepositoryPreferences(1,
                false,
                new ArrayList<>());

        JSONDataLoader loaderMock = mock(JSONDataLoader.class);
        MWLDetails mwlDetails = mock(MWLDetails.class);
        when(mwlDetails.getActiveMWL()).thenReturn(null);
        when(loaderMock.getMwlDetails()).thenReturn(mwlDetails);
        LocalFileHelper fileHelper = mock(LocalFileHelper.class);

        ISettingsProvider settingsProvider = mock(ISettingsProvider.class);
        when(settingsProvider.getCardRepositoryPreferences()).thenReturn(prefs);
        when(settingsProvider.getLanguagePref()).thenReturn("en");
        CardRepository repo = new CardRepository(null, settingsProvider, loaderMock);
        CardPool pool = repo.getGlobalCardPool();

        assertNotNull(pool);
        // assert the preferences were called on getglobalcardpool
    }

    @Test
    public void getCardPool() {
    }
}