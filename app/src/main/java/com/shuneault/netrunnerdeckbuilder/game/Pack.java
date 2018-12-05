package com.shuneault.netrunnerdeckbuilder.game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sebast on 02/07/16.
 */

public class Pack {


    public void setName(String name) {
        this.name = name;
    }

    public void setCycleCode(String code) {
        this.cycle_code = code;
    }

    public static class SetCode {
        public static final String CORE_SET = "core";
        public static final String REVISED_CORE_SET = "core2";
        public static final String SYSTEM_CORE_2019 = "sc19";
    }

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
    private ArrayList<CardLink> cardLinks = new ArrayList<>();

    public Pack() {
    }

    public Pack(JSONObject json) {
        this.code = json.optString(KEY_CODE, "");
        this.cycle_code = json.optString(KEY_CYCLE_CODE, "");
        this.date_release = json.optString(KEY_DATE_RELEASE, "");
        this.name = json.optString(KEY_NAME, "");
        this.position = json.optInt(KEY_POSITION, 0);
        this.size = json.optInt(KEY_SIZE, 0);
        JSONArray cards = json.optJSONArray("cards");
        if (cards != null) {
            for (int i = 0; i < cards.length(); i++) {
                try {
                    JSONObject card;
                    card = cards.getJSONObject(i);
                    String cardCode = card.getString("code");
                    int quantity = card.getInt("quantity");
                    CardLink cl = new CardLink(cardCode, quantity);
                    this.cardLinks.add(cl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getCode() {
        return code;
    }


    public void setCode(String code) {
        this.code = code;
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

    public ArrayList<CardLink> getCardLinks() {return cardLinks; }

    boolean isCoreSet() {
        return code.equals(SetCode.CORE_SET)
                || code.equals(SetCode.REVISED_CORE_SET)
                || code.equals(SetCode.SYSTEM_CORE_2019);
    }
}
