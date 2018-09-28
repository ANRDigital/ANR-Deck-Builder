package com.shuneault.netrunnerdeckbuilder.game;


import org.json.JSONException;
import org.json.JSONObject;

public class CardMWL {
    private int globalPenalty = 0;
    private int universalFactionCost = 0;
    private boolean hasDeckLimit = false;
    private int deckLimit = -1;
    private boolean isRestricted = false;

    public CardMWL() { }

    public CardMWL(JSONObject mwlData) {
        try {
            if (mwlData.has("global_penalty")) {
                int globalPenalties = mwlData.getInt("global_penalty");
                this.globalPenalty = globalPenalties;
            }
            if (mwlData.has("universal_faction_cost")) {
                this.universalFactionCost = mwlData.getInt("universal_faction_cost");
            }
            if (mwlData.has("deck_limit")) {
                this.hasDeckLimit = true;
                this.deckLimit = mwlData.getInt("deck_limit");
            }
            if (mwlData.has("is_restricted")) {
                this.isRestricted = mwlData.getInt("is_restricted") == 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getGlobalPenalty() {
        return globalPenalty;
    }

    public int getUniversalFactionCost() {
        return universalFactionCost;
    }

    public int getDeckLimit() {
        return deckLimit;
    }

    public boolean hasDeckLimit() {
        return hasDeckLimit;
    }

    public boolean isRestricted() {
        return isRestricted;
    }

    public boolean isRemoved() {
        return hasDeckLimit && deckLimit == 0;
    }
}