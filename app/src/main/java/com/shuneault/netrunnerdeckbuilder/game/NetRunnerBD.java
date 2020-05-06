package com.shuneault.netrunnerdeckbuilder.game;

import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

public class NetRunnerBD {

    public static final String BASE_URL = "https://netrunnerdb.com";
    public static final String URL_API_SEARCH = "/api/search/";
    private static final String URL_GET_ALL_CARDS = "https://netrunnerdb.com/api/2.0/public/cards?_locale=%s";
    private static final String URL_GET_ALL_PACKS = "https://netrunnerdb.com/api/2.0/public/packs";
    private static final String URL_GET_MWL = "https://netrunnerdb.com/api/2.0/public/mwl";

    // new github pages data location
//    public static final String URL_CARDS_JSON = "https://anrdigital.github.io/ANR-Deck-Builder/app/src/main/res/raw/cardsv2.json";
//    public static final String URL_PACKS_JSON = "https://anrdigital.github.io/ANR-Deck-Builder/app/src/main/res/raw/packs.json";
//    public static final String URL_MWL_JSON = "https://anrdigital.github.io/ANR-Deck-Builder/app/src/main/res/raw/mwl.json";
    public static final String URL_CYCLES_JSON = "https://anrdigital.github.io/ANR-Deck-Builder/app/src/main/res/raw/cycles.json";
    public static final String URL_FORMATS_JSON = "https://anrdigital.github.io/ANR-Deck-Builder/app/src/main/res/raw/formats.json";
    public static final String URL_ROTATIONS_JSON = "https://anrdigital.github.io/ANR-Deck-Builder/app/src/main/res/raw/rotations.json";

    public static String getAllCardsUrl() {
        return String.format(URL_GET_ALL_CARDS, AppManager.getInstance().getSharedPrefs().getString(SettingsActivity.KEY_PREF_LANGUAGE, "en"));
    }

    public static String getAllPacksUrl() {
        return URL_GET_ALL_PACKS;
    }

    public static String getMWLUrl() {
        return URL_GET_MWL;
    }

    public static String getFormatsUrl() {
        return URL_FORMATS_JSON;
    }

    public static String getCyclesUrl() {
        return URL_CYCLES_JSON;
    }

    public static String getRotationsUrl() {
        return URL_ROTATIONS_JSON;
    }
}
