package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;

import org.json.JSONObject;

import java.util.HashMap;

class MWLDetails {
    private MostWantedList activeMWL;
    private HashMap<String, JSONObject> influences = new HashMap<>();

    public MostWantedList getActiveMWL() {
        return activeMWL;
    }

    public void setActiveMWL(MostWantedList activeMWL) {
        this.activeMWL = activeMWL;
    }

    public HashMap<String, JSONObject> getInfluences() {
        return influences;
    }

    public void setInfluences(HashMap<String, JSONObject> influences) {
        this.influences = influences;
    }
}
