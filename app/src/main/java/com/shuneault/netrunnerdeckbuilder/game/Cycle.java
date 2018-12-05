package com.shuneault.netrunnerdeckbuilder.game;

import org.json.JSONObject;

public class Cycle {
    public static final String KEY_CODE = "code";
    public static final String KEY_ROTATED = "rotated";

    private String code;
    private boolean rotated;

    public Cycle(JSONObject json) {
        this.code = json.optString(KEY_CODE, "");
        this.rotated = json.optBoolean(KEY_ROTATED, false);
    }

    public Cycle() {

    }

    public String getCode() {
        return code;
    }

    public boolean isRotated() {
        return rotated;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setRotation(boolean value) {
        this.rotated = value;
    }
}
