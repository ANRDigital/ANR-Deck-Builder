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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JSONDataLoaderTest {
    final private String formatJSON = "{\n" +
            "  \"data\":[\n" +
            "    {\n" +
            "      \"id\":1,\n" +
            "      \"name\":\"Standard\",\n" +
            "      \"description\":\"All packs, following rotation\",\n" +
            "      \"rotation\":\"rotation-2018\",\n" +
            "      \"mwl\":10\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":2,\n" +
            "      \"name\":\"Snapshot\",\n" +
            "      \"description\":\"All packs, following rotation as at Reign & Reverie\",\n" +
            "      \"rotation\":\"rotation-2017\",\n" +
            "      \"mwl\":9\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":3,\n" +
            "      \"name\":\"Eternal\",\n" +
            "      \"description\":\"All packs, ignoring rotation\",\n" +
            "      \"mwl\":9\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":4,\n" +
            "      \"name\":\"Core Experience\",\n" +
            "      \"description\":\"Latest core set only\",\n" +
            "      \"packs\":[\"sc19\"],\n" +
            "      \"core_count\":1\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\":5,\n" +
            "      \"name\":\"Casual\",\n" +
            "      \"description\":\"No rules\"\n" +
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
        assertEquals(5, formats.size());

        Format format = formats.get(3);
        assertEquals(4, format.getId());
        assertEquals("Core Experience", format.getName());
        assertEquals("", format.getRotation()); // obey rotation (not really relevant)
        assertEquals(0, format.getMwlId()); // no mwl
        assertEquals(1, format.getPacks().size());
    }

    @Test
    public void GetFormats_ReturnsAnArrayList_WithStandard() throws JSONException, IOException {
        JSONDataLoader loader = new JSONDataLoader(helperMock);

        ArrayList<Format> formats = loader.getFormats();
        assertNotNull(formats);
        assertEquals(5, formats.size());

        Format format = formats.get(0);
        assertEquals(1, format.getId());
        assertEquals("Standard", format.getName());
        assertNotEquals("", format.getRotation()); // obey rotation
        assertEquals(10, format.getMwlId()); // 9
        assertEquals(0, format.getPacks().size()); // no pack filter specified
    }
}