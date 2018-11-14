package com.shuneault.netrunnerdeckbuilder.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardBuilder;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Singleton;

@Singleton
public class CardRepository {
    private CardList mCards = new CardList();

    public CardRepository(Context context) {
        try{
            loadCards(context);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadCards(Context context) throws IOException, JSONException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        //get json file
        JSONObject jsonFile = LocalFileHelper.getJSONCardsFile(context);
        String imageUrlTemplate = jsonFile.getString("imageUrlTemplate");
        JSONArray jsonCards = jsonFile.getJSONArray("data");

        mCards.clear();
        for (int i = 0; i < jsonCards.length(); i++) {
            // Create the card and add to the array
            //		Do not load cards from the Alternates set
            JSONObject jsonCard = jsonCards.getJSONObject(i);
            JSONObject jsonLocale = jsonCard.optJSONObject("_locale");
            if (jsonLocale != null) {
                String lanquagePref = preferences.getString(SettingsActivity.KEY_PREF_LANGUAGE, "en");
                JSONObject jsonLocaleProps = jsonLocale.getJSONObject(lanquagePref);
                Iterator<String> iter = jsonLocaleProps.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    jsonCard.put(key, jsonLocaleProps.getString(key));
                }
            }


            CardBuilder cardBuilder = new CardBuilder(imageUrlTemplate);
            Card card = cardBuilder.BuildFromJson(jsonCard);

//            int cardUniversalCost = 0;
//            if (mMWLInfluences.containsKey(card.getCode())) {
//                cardUniversalCost = mMWLInfluences.get(card.getCode()).optInt("universal_faction_cost", 0);
//            }
//            card.setMostWantedInfluence(cardUniversalCost);
            mCards.add(card);
        }
    }

    public List<Card> GetAllCards() {
        return (List<Card>) mCards.clone();
    }

    public List<Card> searchCards(String searchText) {
        String searchtxt = searchText.toLowerCase();
        ArrayList<Card> result = new ArrayList<>();
        for (Card c :
                mCards) {
            if (c.getTitle().toLowerCase().contains(searchtxt)
                    || c.getText().toLowerCase().contains(searchtxt)
                    || c.getSideCode().toLowerCase().contains(searchtxt)
                    || c.getSubtype().toLowerCase().contains(searchtxt)
                    || c.getFactionCode().toLowerCase().contains(searchtxt)){
                result.add(c);
            }
        }
        return result;
    }
}