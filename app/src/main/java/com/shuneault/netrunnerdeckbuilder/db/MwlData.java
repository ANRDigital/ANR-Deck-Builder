package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;

import java.util.ArrayList;
import java.util.HashMap;

class MwlData {
    public ArrayList<MostWantedList> allLists = new ArrayList<>();
    private HashMap<String, Integer> influences = new HashMap<>();

    public HashMap<String, Integer> getInfluences() {
        return influences;
    }

    public ArrayList<MostWantedList> getMWLs() {
        return this.allLists;
    }
}
