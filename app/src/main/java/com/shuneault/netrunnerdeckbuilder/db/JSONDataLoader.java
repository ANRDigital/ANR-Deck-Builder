package com.shuneault.netrunnerdeckbuilder.db;

import androidx.annotation.NonNull;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardBuilder;
import com.shuneault.netrunnerdeckbuilder.game.Cycle;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.game.Rotation;
import com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class JSONDataLoader {
    private LocalFileHelper mLocalFileHelper;

    public JSONDataLoader(LocalFileHelper mLocalFileHelper) {
        this.mLocalFileHelper = mLocalFileHelper;
    }

    @NonNull
    public ArrayList<Card> getCardsFromFile(String lanquagePref) throws IOException, JSONException {
        //get json file

        JSONObject jsonFile = mLocalFileHelper.getJSONCardsFile();
        String imageUrlTemplate = jsonFile.getString("imageUrlTemplate");
        JSONArray jsonCards = jsonFile.getJSONArray("data");

        ArrayList<Card> cards = new ArrayList<>();
        for (int i = 0; i < jsonCards.length(); i++) {
            // Create the card and add to the array
            //		Do not load cards from the Alternates set
            JSONObject jsonCard = jsonCards.getJSONObject(i);
            JSONObject jsonLocale = jsonCard.optJSONObject("_locale");
            if (jsonLocale != null) {
                JSONObject jsonLocaleProps = jsonLocale.getJSONObject(lanquagePref);
                Iterator<String> iter = jsonLocaleProps.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    jsonCard.put(key, jsonLocaleProps.getString(key));
                }
            }

            CardBuilder cardBuilder = new CardBuilder(imageUrlTemplate);
            Card card = cardBuilder.BuildFromJson(jsonCard);
            cards.add(card);
        }
        return cards;
    }


    @NonNull
    public ArrayList<Pack> getPacksFromFile() throws IOException, JSONException {
        ArrayList<Pack> packs = new ArrayList<>();
        JSONObject jsonFile = mLocalFileHelper.getJSONPacksFile();
        JSONArray jsonPacks = jsonFile.getJSONArray("data");
        for (int i = 0; i < jsonPacks.length(); i++) {
            Pack pack = new Pack(jsonPacks.getJSONObject(i));
            packs.add(pack);
        }
        return packs;
    }

    public ArrayList<Cycle> getCyclesFromFile() throws IOException, JSONException {
        ArrayList<Cycle> cycles = new ArrayList<>();
            JSONObject jsonFile = mLocalFileHelper.getJSONCyclesFile();
            JSONArray jsonCycles = jsonFile.getJSONArray("data");
            for (int i = 0; i < jsonCycles.length(); i++) {
                Cycle cycle = new Cycle(jsonCycles.getJSONObject(i));
                cycles.add(cycle);
            }
        return cycles;
    }

    @NonNull
    public MwlData getMwlDetails() throws IOException, JSONException {
        MwlData mwl = new MwlData();

        JSONObject mJsonMWLfile = mLocalFileHelper.getJSON_MWLFile();
        JSONArray mMWLData = mJsonMWLfile.getJSONArray("data");
        for (int i = 0; i < mMWLData.length(); i++) {
            JSONObject mwlJSON = mMWLData.getJSONObject(i);
            mwl.allLists.add(new MostWantedList(mwlJSON));
        }

        return mwl;
    }

    public ArrayList<Format> getFormats() throws IOException, JSONException {
        ArrayList<Format> formats = new ArrayList<>();
        JSONObject jsonFormats = mLocalFileHelper.getJSONFormatsFile();
        JSONArray formatData = jsonFormats.getJSONArray("data");
        for (int i = 0; i < formatData.length(); i++) {
            JSONObject formatJSON = formatData.getJSONObject(i);
            formats.add(new Format(formatJSON));
        }
        return formats;
    }

    public ArrayList<Rotation> getRotations() throws IOException, JSONException {
        ArrayList<Rotation> rotations = new ArrayList<>();
        JSONObject jsonRotations = mLocalFileHelper.getJSONRotationsFile();
        JSONArray rotationsData = jsonRotations.getJSONArray("data");
        for (int i = 0; i < rotationsData.length(); i++) {
            JSONObject rotationData = rotationsData.getJSONObject(i);
            rotations.add(new Rotation(rotationData));
        }
        return rotations;
    }
}
