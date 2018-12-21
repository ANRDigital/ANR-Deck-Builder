package com.shuneault.netrunnerdeckbuilder.game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class Format {
    public static final int FORMAT_STANDARD = 1;
    public static final int FORMAT_ETERNAL = 3;
    private String description;
    private String name;
    private int id;
    private String rotation = "";
    private int mwlId;
    private ArrayList<String> packs = new ArrayList<>();
    private int coreCount = 3;
    private boolean canFilter = true;

    public Format(JSONObject formatJSON) {
        try{
            if (formatJSON.has("id")) {
                this.id = formatJSON.getInt("id");
            }
            if (formatJSON.has("name")) {
                this.name = formatJSON.getString("name");
            }
            if (formatJSON.has("description")) {
                this.description = formatJSON.getString("description");
            }
            if (formatJSON.has("rotation")) {
                this.rotation = formatJSON.getString("rotation");
            }
            if (formatJSON.has("packs")){
                JSONArray packArray = formatJSON.getJSONArray("packs");
                for (int i = 0; i < packArray.length(); i++) {
                    String packCode = packArray.getString(i);
                    this.packs.add(packCode);
                }
            }
            if (formatJSON.has("core_count")) {
                this.coreCount = formatJSON.getInt("core_count");
            }
            if (formatJSON.has("mwl")) {
                this.mwlId = formatJSON.getInt("mwl");
            }
            if (formatJSON.has("filter")) {
                this.canFilter = formatJSON.getBoolean("filter");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Format() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    public int getMwlId() {
        return mwlId;
    }

    public void setMwlId(int mwlId) {
        this.mwlId = mwlId;
    }

    public ArrayList<String> getPacks() {
        return this.packs;
    }

    public int getCoreCount() {
        return this.coreCount;
    }

    public void setPacks(ArrayList<String> packs) {
        this.packs = packs;
    }

    public void setCoreCount(int coreCount) {
        this.coreCount = coreCount;
    }

    @Override
    public String toString(){
        return this.name;
    }

    public boolean canFilter() {
        return this.canFilter;
    }
}
