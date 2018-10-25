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

class LocalFileHelper {
    static JSONObject getJSON_MWLFile(Context context, String filename) throws IOException, JSONException {
        int fallbackResource = R.raw.mwl;
        return getLocalJson(context, filename, fallbackResource);
    }

    static JSONObject getJSONCardsFile(Context context, String filename) throws IOException, JSONException {
        int fallbackResource = R.raw.cardsv2;
        // Load the file in memory and return a JSON array
        return getLocalJson(context, filename, fallbackResource);
    }

    static JSONObject getJSONPacksFile(Context context, String filename) throws IOException, JSONException {
        int fallbackResource = R.raw.packs;
        // Load the file in memory and return a JSON array
        return getLocalJson(context, filename, fallbackResource);
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
