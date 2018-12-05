package com.shuneault.netrunnerdeckbuilder.db;

import android.content.Context;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.CardPool;
import com.shuneault.netrunnerdeckbuilder.game.Cycle;
import com.shuneault.netrunnerdeckbuilder.game.CycleList;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.game.MostWantedList;
import com.shuneault.netrunnerdeckbuilder.game.NetRunnerBD;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider;
import com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper;
import com.shuneault.netrunnerdeckbuilder.helper.StringDownloader;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardRepository {
    private final String mLanguagePref;
    private CardList mCards = new CardList();
    private ArrayList<Pack> mPacks = new ArrayList<>();
    private CycleList mCycles = new CycleList();

    private MostWantedList mActiveMWL;
    private HashMap<String, Integer> mMWLInfluences = new HashMap<>();
    private Context mContext;
    private ISettingsProvider settingsProvider;
    private JSONDataLoader fileLoader;
    private ArrayList<MostWantedList> mMostWantedLists = new ArrayList<>();

    public CardRepository(Context context, ISettingsProvider settingsProvider, JSONDataLoader fileLoader) {
        mContext = context;
        this.settingsProvider = settingsProvider;
        this.fileLoader = fileLoader;

        mLanguagePref = settingsProvider.getLanguagePref();

        try{
            loadMwl();
            // MUST LOAD PACKS BEFORE CARDS
            loadPacks();
            loadCards();
            loadCycles();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadMwl() {
        // Most Wanted List
        try {
            MWLDetails mwl = fileLoader.getMwlDetails();
            mMostWantedLists = mwl.getMWLs();
            mActiveMWL = mwl.getActiveMWL();
            mMWLInfluences.clear();
            mMWLInfluences = (HashMap<String, Integer>) mwl.getInfluences().clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCards() {
        try {
            ArrayList<Card> cards = fileLoader.getCardsFromFile(mLanguagePref, mMWLInfluences);
            mCards.clear();
            mCards.addAll(cards);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPacks() {
        try {
            ArrayList<Pack> packs = fileLoader.getPacksFromFile();

            mPacks.clear();
            mPacks.addAll(packs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCycles() {
        try {
            ArrayList<Cycle> cycles = fileLoader.getCyclesFromFile();

            mCycles.clear();
            mCycles.addAll(cycles);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CardList getAllCards() {
        return (CardList) mCards.clone();
    }

    public List<Card> searchCards(String searchText, CardPool cardPool) {
        String searchtxt = searchText.toLowerCase();
        ArrayList<Card> result = new ArrayList<>();
        for (Card c : cardPool.getCards()) {
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

    public ArrayList<Pack> getPacks(boolean includeRotated) {
        ArrayList<Pack> result = new ArrayList<>();
        for (Pack p :
                mPacks) {
            if (includeRotated || !getCycle(p.getCycleCode()).isRotated()) {
                result.add(p);
            }
        }
        return result;
    }

    public ArrayList<Pack> getPacks(ArrayList<String> packCodes) {
        ArrayList<Pack> result = new ArrayList<>();
        for (String code :
                packCodes) {
            result.add(getPack(code));
        }
        return result;
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
        StringDownloader sdMWL = new StringDownloader(mContext,
                NetRunnerBD.getMWLUrl(),
                LocalFileHelper.FILE_MWL_JSON,
                new StringDownloader.FileDownloaderListener() {

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

    public CardList getCardsFromDataPacksToDisplay(ArrayList<String> packFilter) {
        ArrayList<Pack> packList;
        // deck pack filter?
        if (packFilter.size() > 0) {
            packList = getPacksFromFilter(packFilter);
            return mCards.getPackCards(packList);
        } else {
            // Return all cards if set in the preferences
            CardRepositoryPreferences prefs = getPrefs();
            if (prefs.displayAllPacksPref) {
                return (CardList) mCards.clone();
            }

            // use global filter
            packList = getPacksFromFilter(prefs.globalPackFilter);

            return mCards.getPackCards(packList);
        }
    }

    private CardRepositoryPreferences getPrefs() {
        return settingsProvider.getCardRepositoryPreferences();

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

    public Card getCard(String code) {
        return mCards.getCard(code);
    }

    public CardList getPackCards(String setName) {
        ArrayList<String> packNames = new ArrayList<>();
        packNames.add(setName);
        return mCards.getPackCards(getPacksFromFilter(packNames));
    }

    public void refreshCards() {
        doDownloadMWL();
        doDownloadCards();
    }

    public MostWantedList getActiveMwl() {
        return mActiveMWL;
    }

    public CardPool getGlobalCardPool() {
        CardRepositoryPreferences prefs = getPrefs();
        return getCardPool(prefs.displayAllPacksPref ? new ArrayList<>() : prefs.globalPackFilter);
    }

    public CardPool getCardPool(ArrayList<String> packFilter) {
        ArrayList<Pack> packs = getPacksFromFilter(packFilter);
        return new CardPool(this, getPrefs().coreCount, packs);
    }

    public CardPool getCardPool(Format format) {
        return new CardPool(this, format);
    }

    public boolean hasCards() {
        return mCards.size() > 0;
    }

    public ArrayList<String> getCardTypes(String sideCode, boolean includeIdentity) {
        ArrayList<String> cardTypes = mCards.getCardType(sideCode);
        if (!includeIdentity)
            cardTypes.remove(Card.Type.IDENTITY);
        return cardTypes;
    }


    public CardList getPackCards(Pack p) {
        CardList cards = new CardList();
        for (Card card :
                mCards) {
            if (card.getSetCode().equals(p.getCode())){
                cards.add(card);
            }
        }
        return cards;
    }

    public MostWantedList getMostWantedList(int mwlId) {
        for (MostWantedList mwl:
             this.mMostWantedLists) {
            if (mwl.getId().equals(String.valueOf(mwlId)))
                return mwl;
        }
        return null;
    }

    public static class CardRepositoryPreferences {
        public int coreCount;
        public boolean displayAllPacksPref;
        public ArrayList<String> globalPackFilter;

        public CardRepositoryPreferences(int coreCount, boolean displayAllPacks, ArrayList<String> globalPackFilter) {
            this.coreCount = coreCount;
            this.displayAllPacksPref = displayAllPacks;
            this.globalPackFilter = globalPackFilter;
        }
    }

    private Cycle getCycle(String code) {
        for (Cycle cycle : mCycles) {
            if (cycle.getCode().equals(code)) {
                return cycle;
            }
        }
        return null;
    }
}

