package com.shuneault.netrunnerdeckbuilder.db;

import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;

import java.util.ArrayList;
import java.util.HashMap;

class MwlData {
    public ArrayList<MostWantedList> allLists = new ArrayList<>();

    public ArrayList<MostWantedList> getMWLs() {
        return this.allLists;
    }
}
