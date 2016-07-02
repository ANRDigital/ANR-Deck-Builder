package com.shuneault.netrunnerdeckbuilder.game;

import org.json.JSONObject;

/**
 * Created by sebast on 02/07/16.
 */

public class Pack {
    public static final String KEY_CODE = "code";
    public static final String KEY_CYCLE_CODE = "cycle_code";
    public static final String KEY_DATE_RELEASE = "date_release";
    public static final String KEY_NAME = "name";
    public static final String KEY_POSITION = "position";
    public static final String KEY_SIZE = "size";

    private String code;
    private String cycle_code;
    private String date_release;
    private String name;
    private int position;
    private int size;

    public Pack(JSONObject json) {
        this.code = json.optString(KEY_CODE, "");
        this.cycle_code = json.optString(KEY_CYCLE_CODE, "");
        this.date_release = json.optString(KEY_DATE_RELEASE, "");
        this.name = json.optString(KEY_NAME, "");
        this.position = json.optInt(KEY_POSITION, 0);
        this.size = json.optInt(KEY_SIZE, 0);
    }

    public String getCode() {
        return code;
    }

    public String getCycleCode() {
        return cycle_code;
    }

    public String getDateRelease() {
        return date_release;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public int getSize() {
        return size;
    }
}
