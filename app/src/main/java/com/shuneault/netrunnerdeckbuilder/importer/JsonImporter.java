package com.shuneault.netrunnerdeckbuilder.importer;

import com.shuneault.netrunnerdeckbuilder.game.Deck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sebast on 22/06/16.
 */

public class JsonImporter implements IDeckImporter{

    private ArrayList<Deck> mDecks;

    public JsonImporter(String text) throws JSONException {
        ArrayList<Deck> decks = new ArrayList<>();
        if (isJsonArray(text)) {
            JSONArray jsonArray = new JSONArray(text);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                Deck deck = Deck.fromJSON(json);
                decks.add(deck);
            }
        } else if (isJsonObject(text)) {
            JSONObject json = new JSONObject(text);
            Deck deck = Deck.fromJSON(json);
            decks.add(deck);
        }
        mDecks = decks;
    }

    @Override
    public ArrayList<Deck> toDecks() {
        return mDecks;
    }

    private boolean isJsonObject(String text) {
        try {
            new JSONObject(text);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    private boolean isJsonArray(String text) {
        try {
            new JSONArray(text);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }


}
