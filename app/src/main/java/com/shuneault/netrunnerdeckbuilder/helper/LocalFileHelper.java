package com.shuneault.netrunnerdeckbuilder.helper;

import android.content.Context;
import android.support.annotation.NonNull;

import com.shuneault.netrunnerdeckbuilder.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LocalFileHelper {
    public static final String FILE_CARDS_JSON = "cardsv2.json";
    public static final String FILE_PACKS_JSON = "packs.json";
    public static final String FILE_MWL_JSON = "mwl.json";

    public static JSONObject getJSON_MWLFile(Context context) throws IOException, JSONException {
        int fallbackResource = R.raw.mwl;
        return getLocalJson(context, FILE_MWL_JSON, fallbackResource);
    }

    public static JSONObject getJSONCardsFile(Context context) throws IOException, JSONException {
        int fallbackResource = R.raw.cardsv2;
        // Load the file in memory and return a JSON array
        return getLocalJson(context, FILE_CARDS_JSON, fallbackResource);
    }

    public static JSONObject getJSONPacksFile(Context context) throws IOException, JSONException {
        int fallbackResource = R.raw.packs;
        // Load the file in memory and return a JSON array
        return getLocalJson(context, FILE_PACKS_JSON, fallbackResource);
    }

    @NonNull
    private static JSONObject getLocalJson(Context context, String filename, int fallbackResource) throws IOException, JSONException {
        // Load the file in memory and return a JSON array
        InputStream in;
        try {
            in = context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            in = context.getResources().openRawResource(fallbackResource);
        }
        return getJsonObject(in);
    }

    @NonNull
    private static JSONObject getJsonObject(InputStream in) throws IOException, JSONException {
        InputStreamReader fs = new InputStreamReader(in);
        BufferedReader bfs = new BufferedReader(fs);
        String theLine = null;
        StringBuilder theStringBuilder = new StringBuilder();
        // Read the file
        while ((theLine = bfs.readLine()) != null)
            theStringBuilder.append(theLine);

        JSONObject jsonFile = new JSONObject(theStringBuilder.toString());
        bfs.close();
        fs.close();
        in.close();
        return jsonFile;
    }
}
