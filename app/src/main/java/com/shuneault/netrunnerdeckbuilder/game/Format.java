package com.shuneault.netrunnerdeckbuilder.game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class Format {
    private String description;
    private String name;
    private int id;
    private boolean rotation;
    private int mwlId;
    private ArrayList<String> packs = new ArrayList<>();
    private int coreCount = 3;

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
                this.rotation = formatJSON.getBoolean("rotation");
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

    public boolean getRotation() {
        return rotation;
    }

    public void setRotation(boolean rotation) {
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
}
