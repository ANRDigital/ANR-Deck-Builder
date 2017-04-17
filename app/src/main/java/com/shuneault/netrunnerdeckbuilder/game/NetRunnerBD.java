package com.shuneault.netrunnerdeckbuilder.game;

import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

public class NetRunnerBD {

    public static final String BASE_URL = "https://netrunnerdb.com";
    public static final String URL_API_SEARCH = "/api/search/";
    private static final String URL_GET_ALL_CARDS = "https://netrunnerdb.com/api/2.0/public/cards?_locale=%s";
    private static final String URL_GET_ALL_PACKS = "https://netrunnerdb.com/api/2.0/public/packs";
    private static final String URL_GET_MWL = "https://netrunnerdb.com/api/2.0/public/mwl";

    public static String getAllCardsUrl() {
        return String.format(URL_GET_ALL_CARDS, AppManager.getInstance().getSharedPrefs().getString(SettingsActivity.KEY_PREF_LANGUAGE, "en"));
    }

    public static String getAllPacksUrl() {
        return URL_GET_ALL_PACKS;
    }

    public static String getMWLUrl() { return URL_GET_MWL; }
}
