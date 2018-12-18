package com.shuneault.netrunnerdeckbuilder.game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Rotation {
    private String name = "";
    private ArrayList<String> cycles = new ArrayList<>();
    private String code = "";

    public Rotation(JSONObject rotationJSON) {

        try {
            this.code = rotationJSON.getString("code");
            this.name = rotationJSON.getString("name");
            JSONArray packArray = rotationJSON.getJSONArray("cycles");
            for (int i = 0; i < packArray.length(); i++) {
                String cycleCode = packArray.getString(i);
                this.cycles.add(cycleCode);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Rotation() {

    }

    public String getCode() {
        return this.code;
    }

    public ArrayList<String> getCycles() {
        return cycles;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
