package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.CardCount;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

class MWLDetails {
    public ArrayList<MostWantedList> allLists = new ArrayList<>();
    private MostWantedList activeMWL;
    private HashMap<String, Integer> influences = new HashMap<>();

    public MostWantedList getActiveMWL() {
        return activeMWL;
    }

    public void setActiveMWL(MostWantedList activeMWL) {
        this.activeMWL = activeMWL;
    }

    public HashMap<String, Integer> getInfluences() {
        return influences;
    }

    public void setInfluences(HashMap<String, Integer> influences) {
        this.influences = influences;
    }

    public ArrayList<MostWantedList> getMWLs() {
        return this.allLists;
    }
}
