package com.shuneault.netrunnerdeckbuilder.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardBuilder;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.game.NetRunnerBD;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper;
import com.shuneault.netrunnerdeckbuilder.helper.StringDownloader;
import com.shuneault.netrunnerdeckbuilder.prefs.ListPreferenceMultiSelect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CardRepository {
    private CardList mCards = new CardList();
    private ArrayList<Pack> mPacks = new ArrayList<>();

    private MostWantedList mActiveMWL;
    private HashMap<String, JSONObject> mMWLInfluences = new HashMap<>();
    private Context mContext;

    public CardRepository(Context context) {
        mContext = context;
        try{
            loadMwl();
            // MUST LOAD PACKS BEFORE CARDS
            loadPacks();
            loadCards();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadMwl() {
        // Most Wanted List
        JSONObject mJsonMWLfile = null;
        try {
            mJsonMWLfile = LocalFileHelper.getJSON_MWLFile(mContext);
            JSONArray mMWLData = mJsonMWLfile.getJSONArray("data");
            for (int i = 0; i < mMWLData.length(); i++) {
                JSONObject mwlJSON = mMWLData.getJSONObject(i);
                if (mwlJSON.has("active")) {
                    mActiveMWL = new MostWantedList(mwlJSON);
                }
            }

            mMWLInfluences.clear();
            JSONObject jsonMWLCards = mMWLData
                    .getJSONObject(2)
                    .getJSONObject("cards");
            Iterator<String> iterCards = jsonMWLCards.keys();
            while (iterCards.hasNext()) {
                String cardCode = iterCards.next();
                mMWLInfluences.put(cardCode, jsonMWLCards.getJSONObject(cardCode));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCards() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        try {
            //get json file
            JSONObject jsonFile = LocalFileHelper.getJSONCardsFile(mContext);
            String imageUrlTemplate = jsonFile.getString("imageUrlTemplate");
            JSONArray jsonCards = jsonFile.getJSONArray("data");

            mCards.clear();
            for (int i = 0; i < jsonCards.length(); i++) {
                // Create the card and add to the array
                //		Do not load cards from the Alternates set
                JSONObject jsonCard = jsonCards.getJSONObject(i);
                JSONObject jsonLocale = jsonCard.optJSONObject("_locale");
                if (jsonLocale != null) {
                    String lanquagePref = preferences.getString(SettingsActivity.KEY_PREF_LANGUAGE, "en");
                    JSONObject jsonLocaleProps = jsonLocale.getJSONObject(lanquagePref);
                    Iterator<String> iter = jsonLocaleProps.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        jsonCard.put(key, jsonLocaleProps.getString(key));
                    }
                }

                CardBuilder cardBuilder = new CardBuilder(imageUrlTemplate, this);
                Card card = cardBuilder.BuildFromJson(jsonCard);

                int cardUniversalCost = 0;
                if (mMWLInfluences.containsKey(card.getCode())) {
                    cardUniversalCost = mMWLInfluences.get(card.getCode()).optInt("universal_faction_cost", 0);
                }
                card.setMostWantedInfluence(cardUniversalCost);
                mCards.add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPacks() {
        try {
            JSONObject jsonFile = LocalFileHelper.getJSONPacksFile(mContext);
            JSONArray jsonPacks = jsonFile.getJSONArray("data");
            mPacks.clear();
            for (int i = 0; i < jsonPacks.length(); i++) {
                Pack pack = new Pack(jsonPacks.getJSONObject(i));
                mPacks.add(pack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CardList getAllCards() {
        return (CardList) mCards.clone();
    }

    public List<Card> searchCards(String searchText, ArrayList<String> mPackFilter) {
        String searchtxt = searchText.toLowerCase();
        ArrayList<Card> result = new ArrayList<>();
        for (Card c : getCardsFromDataPacksToDisplay(mPackFilter)) {
            if (c.getTitle().toLowerCase().contains(searchtxt)
                    || c.getText().toLowerCase().contains(searchtxt)
                    || c.getSideCode().toLowerCase().contains(searchtxt)
                    || c.getSubtype().toLowerCase().contains(searchtxt)
                    || c.getFactionCode().toLowerCase().contains(searchtxt)){
                result.add(c);
            }
        }
        return result;
    }

    public ArrayList<Pack> getPacks() {
        return mPacks;
    }

    public Pack getPack(String code) {
        for (Pack pack : mPacks) {
            if (pack.getCode().equals(code)) {
                return pack;
            }
        }
        return null;
    }

    public ArrayList<String> getPackNames() {
        ArrayList<String> packNames = new ArrayList<String>();
        for (Pack pack : mPacks) {
            packNames.add(pack.getName());
        }
        return packNames;
    }

    private void doDownloadMWL(){
        // Most Wanted List
        StringDownloader sdMWL = new StringDownloader(mContext, NetRunnerBD.getMWLUrl(), LocalFileHelper.FILE_MWL_JSON, new StringDownloader.FileDownloaderListener() {

            @Override
            public void onBeforeTask() {

            }

            @Override
            public void onTaskComplete(String s) {
                loadMwl();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        sdMWL.execute();

    }

    private void doDownloadCards() {
        // Cards List
        StringDownloader sdCards = new StringDownloader(mContext, NetRunnerBD.getAllCardsUrl(),
                LocalFileHelper.FILE_CARDS_JSON, new StringDownloader.FileDownloaderListener() {
            @Override
            public void onBeforeTask() {

            }

            @Override
            public void onTaskComplete(String s) {
                // update last download date
//                getSharedPrefs()
//                        .edit()
//                        .putLong(SHARED_PREF_LAST_UPDATE_DATE, Calendar.getInstance().getTimeInMillis())
//                        .apply();
                loadCards();
                Toast.makeText(mContext, R.string.card_list_updated_successfully, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        sdCards.execute();

        StringDownloader sdPacks = new StringDownloader(mContext, NetRunnerBD.getAllPacksUrl(), LocalFileHelper.FILE_PACKS_JSON, new StringDownloader.FileDownloaderListener() {
            @Override
            public void onBeforeTask() {

            }

            @Override
            public void onTaskComplete(String s) {
                loadPacks();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        sdPacks.execute();

    }

    public CardList getCardsFromDataPacksToDisplay() {
        return getCardsFromDataPacksToDisplay(new ArrayList<>());
    }

    public CardList getCardsFromDataPacksToDisplay(ArrayList<String> packFilter) {
        ArrayList<Pack> packList;
        // deck pack filter?
        if (packFilter.size() > 0) {
            packList = getPacksFromFilter(packFilter);
            return mCards.getPackCards(packFilter, packList);
        } else {
            // Return all cards if set in the preferences
            if (getSharedPrefs().getBoolean(SettingsActivity.KEY_PREF_DISPLAY_ALL_DATA_PACKS, true)) {
                return (CardList) mCards.clone();
            }

            // Return only the data packFilter requested
            String packsPref = getSharedPrefs().getString(SettingsActivity.KEY_PREF_DATA_PACKS_TO_DISPLAY, "");
            ArrayList<String> globalPackFilter = new ArrayList<>(Arrays.asList(ListPreferenceMultiSelect.parseStoredValue(packsPref)));
            packList = getPacksFromFilter(globalPackFilter);

            return mCards.getPackCards(globalPackFilter, packList);
        }
    }

    private ArrayList<Pack> getPacksFromFilter(ArrayList<String> packFilter) {
        ArrayList<Pack> result = new ArrayList<>();
        for (Pack pack :
                mPacks) {
            if (packFilter.contains(pack.getName())){
                result.add(pack);
            }
        }
        return result;
    }

    public SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }
    public Card getCard(String code) {
        return mCards.getCard(code);
    }

    public CardList getPackCards(String setName) {
        ArrayList<String> arr = new ArrayList<>();
        arr.add(setName);
        return getCardsFromDataPacksToDisplay(arr);
    }

    public void refreshCards() {
        doDownloadMWL();
        doDownloadCards();
    }

    public MostWantedList getActiveMwl() {
        return mActiveMWL;
    }
}