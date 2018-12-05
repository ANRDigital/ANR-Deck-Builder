package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JSONDataLoaderTest {
    final private String formatJSON = "{\n" +
            "  \"data\":[\n" +
            "    {\n" +
            "      \"id\":1,\n" +
            "      \"name\":\"Core Experience\",\n" +
            "      \"description\":\"Latest core set only\",\n" +
            "      \"packs\":[\"sc19\"],\n" +
            "      \"core_count\":1,\n" +
            "      \"rotation\":true,\n" +
            "      \"mwl\":0\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":2,\n" +
            "      \"name\":\"Standard\",\n" +
            "      \"description\":\"All packs, following rotation\",\n" +
            "      \"rotation\":true,\n" +
            "      \"mwl\":9\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    private LocalFileHelper helperMock;

    @Before
    public void Setup() throws JSONException, IOException {
        helperMock = mock(LocalFileHelper.class);

        JSONObject value = new JSONObject(formatJSON);
        when(helperMock.getJSONFormatsFile()).thenReturn(value);

    }

    @Test
    public void GetFormats_ReturnsAnArrayList_WithCoreExperience() throws JSONException, IOException {
        JSONDataLoader loader = new JSONDataLoader(helperMock);

        ArrayList<Format> formats = loader.getFormats();
        assertNotNull(formats);
        assertEquals(2, formats.size());

        Format format = formats.get(0);
        assertEquals(1, format.getId());
        assertEquals("Core Experience", format.getName());
        assertTrue(format.getRotation()); // obey rotation (not really relevant)
        assertEquals(0, format.getMwlId()); // no mwl
        assertEquals(1, format.getPacks().size());
    }

    @Test
    public void GetFormats_ReturnsAnArrayList_WithStandard() throws JSONException, IOException {
        JSONDataLoader loader = new JSONDataLoader(helperMock);

        ArrayList<Format> formats = loader.getFormats();
        assertNotNull(formats);
        assertEquals(2, formats.size());

        Format format = formats.get(1);
        assertEquals(2, format.getId());
        assertEquals("Standard", format.getName());
        assertTrue(format.getRotation()); // obey rotation
        assertEquals(9, format.getMwlId()); // 9
        assertEquals(0, format.getPacks().size()); // no pack filter specified
    }
}