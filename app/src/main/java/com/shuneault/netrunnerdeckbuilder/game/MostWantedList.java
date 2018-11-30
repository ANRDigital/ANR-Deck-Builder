package com.shuneault.netrunnerdeckbuilder.game;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class MostWantedList {
    private String code;
    private String name = "";
    private String iD = "";
    private boolean active = false;
    private HashMap<String, CardMWL> cards = new HashMap<>();

    public MostWantedList(JSONObject mwlData) {
        try {
            if (mwlData.has("id")) {
                this.iD = mwlData.getString("id");
            }
            if (mwlData.has("code")) {
                this.code = mwlData.getString("code");
            }
            if (mwlData.has("name")) {
                this.name = mwlData.getString("name");
            }
            if (mwlData.has("active")) {
                this.active = mwlData.getBoolean("active");
            }

            JSONObject jsonMWLCards = mwlData.getJSONObject("cards");
            Iterator<String> iterCards = jsonMWLCards.keys();
            while (iterCards.hasNext()) {
                String cardCode = iterCards.next();
                JSONObject jsonCard = jsonMWLCards.getJSONObject(cardCode);
                this.cards.put(cardCode, new CardMWL(jsonCard));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public CardMWL GetCardMWL(Card card) {
        if (this.cards.containsKey(card.getCode())) {
            return cards.get(card.getCode());
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }
}
