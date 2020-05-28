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
import com.shuneault.netrunnerdeckbuilder.game.Rotation;
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider;
import com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper;
import com.shuneault.netrunnerdeckbuilder.helper.StringDownloader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CardRepository {
    private final String mLanguagePref;
    private CardList mCards = new CardList();
    private ArrayList<Pack> mPacks = new ArrayList<>();
    private CycleList mCycles = new CycleList();

    private Context mContext;
    private ISettingsProvider settingsProvider;
    private JSONDataLoader fileLoader;
    private ArrayList<MostWantedList> mMostWantedLists = new ArrayList<>();
    private ArrayList<Format> mFormats = new ArrayList<>();
    private ArrayList<Rotation> mRotations = new ArrayList<>();

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
            loadRotations();
            loadFormats();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadFormats() {
        try {
            ArrayList<Format> formats = fileLoader.getFormats();

            mFormats.clear();
            mFormats.addAll(formats);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRotations() {
        try {
            ArrayList<Rotation> rotations = fileLoader.getRotations();

            mRotations.clear();
            mRotations.addAll(rotations);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMwl() {
        // Most Wanted List
        try {
            MwlData mwl = fileLoader.getMwlDetails();
            mMostWantedLists = mwl.getMWLs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCards() {
        try {
            ArrayList<Card> cards = fileLoader.getCardsFromFile(mLanguagePref);
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

    public ArrayList<Pack> getPacks(Rotation rotation) {
        if (rotation == null)
        {
            return mPacks;
        }
        ArrayList<Pack> result = new ArrayList<>();
        for (String cycleCode : rotation.getCycles()){
            for (Pack p : mPacks) {
                if (p.getCycleCode().equals(cycleCode))
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

    private void doDownloadFormats(){
        // Game Formats
        StringDownloader sdFormats = new StringDownloader(mContext,
                NetRunnerBD.getFormatsUrl(),
                LocalFileHelper.FILE_FORMATS_JSON,
                new StringDownloader.FileDownloaderListener() {

                    @Override
                    public void onBeforeTask() {

                    }

                    @Override
                    public void onTaskComplete(String s) {
                        loadFormats();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
        sdFormats.execute();
    }

    private void doDownloadCycles(){
        // Cycles
        StringDownloader sdCycles = new StringDownloader(mContext,
                NetRunnerBD.getCyclesUrl(),
                LocalFileHelper.FILE_CYCLES_JSON,
                new StringDownloader.FileDownloaderListener() {

                    @Override
                    public void onBeforeTask() {

                    }

                    @Override
                    public void onTaskComplete(String s) {
                        loadCycles();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
        sdCycles.execute();
    }
    private void doDownloadRotations(){
        // Cycles
        StringDownloader sdCycles = new StringDownloader(mContext,
                NetRunnerBD.getRotationsUrl(),
                LocalFileHelper.FILE_ROTATIONS_JSON,
                new StringDownloader.FileDownloaderListener() {

                    @Override
                    public void onBeforeTask() {

                    }

                    @Override
                    public void onTaskComplete(String s) {
                        loadRotations();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
        sdCycles.execute();
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

    private CardRepositoryPreferences getPrefs() {
        return settingsProvider.getCardRepositoryPreferences();

    }

    public ArrayList<Pack> getPacksFromCodes(ArrayList<String> codes) {
        ArrayList<Pack> result = new ArrayList<>();
        for (Pack pack : mPacks) {
            if (codes.contains(pack.getCode())){
                result.add(pack);
            }
        }
        return result;
    }

    private ArrayList<Pack> getPacksFromNames(ArrayList<String> names) {
        ArrayList<Pack> result = new ArrayList<>();
        for (Pack pack : mPacks) {
            if (names.contains(pack.getName())){
                result.add(pack);
            }
        }
        return result;
    }

    public Card getCard(String code) {
        return mCards.getCard(code);
    }

    public CardList getPackCards(String code) {
        Pack pack = getPack(code);
        ArrayList<Pack> packs = new ArrayList<>();
        packs.add(pack);
        return mCards.getPackCards(packs);
    }

    public void doDownloadAllData() {
        doDownloadCycles();
        doDownloadRotations();
        doDownloadFormats();
        doDownloadMWL();
        doDownloadCards();
    }

    // this called by card browser
    public CardPool getCardPool() {
        Format defaultFormat = getDefaultFormat();
        return getCardPool(defaultFormat);
    }

    // this called by non-deck sources (identity)
    public CardPool getCardPool(Format format){
        return getCardPool(format, null, 0); // no deck / filter overrides
    }

    // this called by card browser
    public CardPool getCardPool(Format format, ArrayList<String> packFilter){
        return getCardPool(format, packFilter, 0); // no deck / filter overrides
    }

    // this called by all deck sources
    public CardPool getCardPool(Format format, ArrayList<String> packFilter, int deckCoreCount) {
        ArrayList<Pack> packs = getPacks(format, packFilter);
        Rotation rotation = this.getRotation(format.getRotation());
        int coreCount = format.getCoreCount();
        if (deckCoreCount > 0) {
            coreCount = deckCoreCount;
        }
        return new CardPool(this, packs, coreCount, rotation);
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
        for (Card card : mCards) {
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

    /**
     * Returns the requested game format, or the default format if not found
     * @param formatId  the id of the format to get
     * @return          the Format object with the specified id
     */
    public Format getFormat(int formatId) {
        for (Format f :
                mFormats) {
            if (f.getId() == formatId)
                return f;
        }
        return getDefaultFormat();
    }

    public Format getDefaultFormat() {
        int formatId = settingsProvider.getDefaultFormatId();
        for (Format f :
                mFormats) {
            if (f.getId() == formatId)
                return f;
        }
        return mFormats.get(0); // return the first if not set, or fall over if formats didn't load.
    }

    public ArrayList<Format> getFormats() {
        return mFormats;
    }

    public Rotation getRotation(String code) {
        for (Rotation rotation : mRotations) {
            if (rotation.getCode().equals(code)) {
                return rotation;
            }
        }
        return null;
    }

    public ArrayList<Pack> getPacks(Format format, ArrayList<String> packFilter) {
        // is the format for a limited pack set?
        ArrayList<Pack> result = getPacks(format.getPacks());
        if  (result.size() == 0){
            // no limited set so check for rotation
            Rotation rotation = getRotation(format.getRotation());
            if (rotation != null){
                // load specified packs from rotation
                ArrayList<String> cycleCodes = rotation.getCycles();
                for (Pack p: mPacks){
                    if (cycleCodes.contains(p.getCycleCode()))
                    {
                        result.add(p);
                    }
                }
            }
            else
            {
                result.addAll(mPacks);
            }
        }

        // then filter the results if there's a pack filter
        if (format.canFilter() && packFilter != null && packFilter.size() > 0) {
            Iterator<Pack> iter = result.iterator();

            while (iter.hasNext()) {
                Pack p = iter.next();

                if (!packFilter.contains(p.getCode()))
                    iter.remove();
            }
        }

        return result;
    }

    public ArrayList<Pack> getPacks(Format format) {
        return getPacks(format, new ArrayList<>());
    }

    public ArrayList<Card> getCards(ArrayList<String> cardCodes) {
        ArrayList<Card> cards = new ArrayList<>();
        for (String code : cardCodes) {
            cards.add(mCards.getCard(code));
        }
        return cards;
    }

    public static class CardRepositoryPreferences {
        public int coreCount;

        public CardRepositoryPreferences(int coreCount) {
            this.coreCount = coreCount;
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

