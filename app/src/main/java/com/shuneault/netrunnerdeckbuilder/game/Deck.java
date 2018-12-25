package com.shuneault.netrunnerdeckbuilder.game;

import android.text.TextUtils;

import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.adapters.HeaderListItemInterface;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import static com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper.PACK_FILTER_SEPARATOR;

public class Deck implements Serializable, HeaderListItemInterface {

    private static final String ReservedChars = "[\\|\\?\\*\\<\\\"\\:\\>\\+\\[\\]\\/\\\\\\']";

    private Card mIdentity;
    private String mName = "";
    private String mNotes = "";
    private boolean mStarred = false;
    private UUID mUUID = UUID.randomUUID();
    private Long mRowId;
    private HashMap<Card, Integer> mCards = new HashMap<Card, Integer>();

    // Cards to add and cards to remove for deck building
    private HashMap<Card, CardCount> mCardsToAdd = new HashMap<Card, CardCount>();
    private HashMap<Card, CardCount> mCardsToRemove = new HashMap<Card, CardCount>();
    private ArrayList<CardCount> mArrCardsToAdd = new ArrayList<CardCount>();
    private ArrayList<CardCount> mArrCardsToRemove = new ArrayList<CardCount>();

    // JSON Values
    public static final String JSON_DECK_UUID = "deck_uuid";
    public static final String JSON_DECK_NAME = "deck_name";
    public static final String JSON_DECK_NOTES = "deck_notes";
    public static final String JSON_DECK_STARRED = "deck_starred";
    public static final String JSON_DECK_IDENTITY_CODE = "deck_identity_code";
    public static final String JSON_DECK_CARD_CODE = "card_code";
    public static final String JSON_DECK_CARD_COUNT = "card_count";
    public static final String JSON_DECK_CARDS = "cards";
    public static final String JSON_DECK_CARDS_TO_ADD = "cards_to_add";
    public static final String JSON_DECK_CARDS_TO_REMOVE = "cards_to_remove";
    public static final String JSON_DECK_CARDS_DONE = "is_done";
    public static final String JSON_DECK_FORMAT = "format";
    public static final String JSON_DECK_PACK_FILTER = "pack_filter";
    public static final String JSON_DECK_CORE_COUNT = "core_count";

    // Rules values
    public static final int BASE_AGENDA = 2;

    private static final long serialVersionUID = 2114649051205735605L;
    private boolean hasUnknownCards = false;
    private Format format;
    private ArrayList<String> packFilter = new ArrayList<>();
    private int coreCount = 0; // 0 indicates no override

    public Deck(Card identity, Format format) {
        this.mIdentity = identity;
        this.format = format;
    }

    public String getName() {
        return mName;
    }

    public String getFileSafeName() {
        return getName().replaceAll(ReservedChars, "_");
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Card getIdentity() {
        return mIdentity;
    }

    public void setIdentity(Card card) {
        mIdentity = card;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }


    public String getSide() {
        try {
            return mIdentity.getSideCode();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public int getItemType() {
        return HeaderListItemInterface.TYPE_ITEM;
    }

    @Override
    public String getItemName() {
        return this.getName();
    }


    public void updateCardCount(Card card, int count) {
        // Add or remove the card count
        int iCountToAdd = (mCardsToAdd.get(card) == null ? 0 : mCardsToAdd.get(card).getCount());
        int iCountToRemove = (mCardsToRemove.get(card) == null ? 0 : mCardsToRemove.get(card).getCount());
        int iCountOriginal = (mCards.get(card) == null ? 0 : mCards.get(card)) + -iCountToAdd - iCountToRemove;
        mCardsToAdd.remove(card);
        mCardsToRemove.remove(card);
        if (iCountOriginal != count) {
            // Add or remove
            if (iCountOriginal > count) // We removed some cards
                mCardsToRemove.put(card, new CardCount(card, count - iCountOriginal));
            else
                mCardsToAdd.put(card, new CardCount(card, count - iCountOriginal));
        }

        // Regenerate the arrays
        getCardsToAdd();
        getCardsToRemove();

        setCardCount(card, count);
    }

    public void setCardCount(Card card, int count) {
        // Modify the deck
        mCards.remove(card);
        if (count > 0)
            mCards.put(card, count);
    }

    public ArrayList<Card> getCards() {
        ArrayList<Card> cardList = new ArrayList<Card>();
        for (Card card : mCards.keySet())
            cardList.add(card);
        return cardList;
    }

    public Integer getCardCount(Card card) {
        Integer iCount = mCards.get(card);
        if (iCount == null)
            return 0;
        else
            return iCount;
    }

    public int getCardCountByType(String type) {
        int iCount = 0;
        for (Card card : mCards.keySet()) {
            if (card.getTypeCode().contains(type)) {
                iCount = iCount + getCardCount(card);
            }
        }
        return iCount;
    }

    public int getCardCountByFaction(String faction) {
        int iCount = 0;
        for (Card card : mCards.keySet()) {
            if (card.getFactionCode().equals(faction)) {
                iCount = iCount + getCardCount(card);
            }
        }
        return iCount;
    }

    public int getCardCountBySubTypeAndFaction(String subtype, String faction) {
        int iCount = 0;
        for (Card card : mCards.keySet()) {
            if (card.getSubtype().contains(subtype) && card.getFactionCode().equals(faction)) {
                iCount = iCount + getCardCount(card);
            }
        }
        return iCount;
    }

    public ArrayList<CardCount> getCardsToAdd() {
        mArrCardsToAdd.clear();
        for (Card card : mCardsToAdd.keySet())
            mArrCardsToAdd.add(mCardsToAdd.get(card));
        for (Card card : mCardsToRemove.keySet())
            mArrCardsToAdd.add(mCardsToRemove.get(card));
        return mArrCardsToAdd;
    }

    public ArrayList<CardCount> getCardsToRemove() {
        mArrCardsToRemove.clear();
        for (Card card : mCardsToRemove.keySet())
            mArrCardsToRemove.add(mCardsToRemove.get(card));
        return mArrCardsToRemove;
    }

    public int getCardsToAddCount(Card card) {
        Integer iCount = mCardsToAdd.get(card).getCount();
        return (iCount == null ? 0 : iCount);
    }

    public int getCardsToRemoveCount(Card card) {
        Integer iCount = mCardsToRemove.get(card).getCount();
        return (iCount == null ? 0 : iCount);
    }

    public int getDeckSize() {
        int iDeckSize = 0;
        for (Card card : getCards()) {
            iDeckSize = iDeckSize + getCardCount(card);
        }
        return iDeckSize;
    }

    public int getDeckInfluence() {
        int iInfluence = 0;
        // Most Wanted List
        if (AppManager.getInstance().getSharedPrefs().getBoolean(SettingsActivity.KEY_PREF_USE_MOST_WANTED_LIST, false)) {
            for (Card card : this.getCards()) {
                iInfluence = iInfluence + (getCardCount(card) * card.getMWLInfluence());
            }
        }

        // IDENTITY: The Professor (03029) does count influence diffently
        if (mIdentity.getCode().equals(Card.SpecialCards.CARD_THE_PROCESSOR)) {
            for (Card card : getCards()) {
                if (!mIdentity.getFactionCode().equals(card.getFactionCode())) {
                    if (card.getTypeCode().equals(Card.Type.PROGRAM)) {
                        // First copy of each program does not count toward the influence value
                        iInfluence = iInfluence + (card.getFactionCost() * Math.max(getCardCount(card) - 1, 0));
                    } else {
                        iInfluence = iInfluence + (card.getFactionCost() * getCardCount(card));
                    }
                }
            }
        } else {
            for (Card card : getCards()) {
                if (!mIdentity.getFactionCode().equals(card.getFactionCode())) {

                    // Some cards have a different influence based on the text
                    switch (card.getCode()) {
                        case "10018":  // Mumba Temple - 0 influence if 15 ice or less in deck
                            if (getCardCountByType(Card.Type.ICE) <= 15) {
                                continue;
                            }
                            break;
                        case "10019":  // Museum of History - 0 influence if 50 or more cards in deck
                            if (getDeckSize() >= 50) {
                                continue;
                            }
                            break;
                        case "10029":   // Product Recall       - 0 influence if 6 or more non-alliance HB cards in deck
                        case "10067":  // Jeeves Model Bioroids - 0 influence if 6 or more non-alliance HB cards in deck
                            if (getCardCountByFaction(Card.Faction.FACTION_HAAS_BIOROID) - getCardCountBySubTypeAndFaction(Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_HAAS_BIOROID) >= 6) {
                                continue;
                            }
                            break;
                        case "10068":  // Raman Rai - 0 influence if 6 or more non-alliance Jinteki cards in deck
                            if (getCardCountByFaction(Card.Faction.FACTION_JINTEKI) - getCardCountBySubTypeAndFaction(Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_JINTEKI) >= 6) {
                                continue;
                            }
                            break;
                        case "10109":  // Ibrahim Salem - 0 influence if 6 or more non-alliance NBN cards in deck
                            if (getCardCountByFaction(Card.Faction.FACTION_NBN) - getCardCountBySubTypeAndFaction(Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_NBN) >= 6) {
                                continue;
                            }
                            break;
                        case "10094":   // Consulting Visit - 0 influence if 6 or more non-alliance Weyland cards in deck
                        case "10072":  // Executive search firm - 0 influence if 6 or more non-alliance Weyland cards in deck
                            if (getCardCountByFaction(Card.Faction.FACTION_WEYLAND_CONSORTIUM) - getCardCountBySubTypeAndFaction(Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_WEYLAND_CONSORTIUM) >= 6) {
                                continue;
                            }
                            break;
                        case "10013":  // Heritage Committee - 0 influence if 6 or more non-alliance Jinteki cards in deck
                            if (getCardCountByFaction(Card.Faction.FACTION_JINTEKI) - getCardCountBySubTypeAndFaction(Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_JINTEKI) >= 6) {
                                continue;
                            }
                            break;
                        case "10071":  // Salem's Hospitality - 0 influence if 6 or more non-alliance NBN cards in deck
                            if (getCardCountByFaction(Card.Faction.FACTION_NBN) - getCardCountBySubTypeAndFaction(Card.SubTypeCode.ALLIANCE, Card.Faction.FACTION_NBN) >= 6) {
                                continue;
                            }
                            break;
                        case "10076":  // Mumbad Virtual tour - 0 influence if you have 7 or more assets in your deck
                            if (getCardCountByType(Card.Type.ASSET) >= 7) {
                                continue;
                            }
                            break;
                        case "10038":   // PAD Factory - 0 influence if the deck contains 3 PAD Campains cards in deck
                            if (getCardCount(AppManager.getInstance().getCard("01109")) == 3) {
                                continue;
                            }
                            break;
                    }

                    iInfluence = iInfluence + (card.getFactionCost() * getCardCount(card));
                }
            }
        }
        return iInfluence;
    }

    public int getMinimumDeckSize() {
        return mIdentity.getMinimumDeckSize();
    }

    public int getInfluenceLimit() {
        int influenceLimit = mIdentity.getInfluenceLimit();
        return influenceLimit;
    }

    public int getDeckAgenda() {
        int iAgendaPoints = 0;
        for (Card card : getCards()) {
            iAgendaPoints = iAgendaPoints + (card.getAgendaPoints() * getCardCount(card));
        }
        return iAgendaPoints;
    }

    // Returns the maximum between the minimum agenda based on min deck size
    //	or the agenda requirement based on the current deck size
    public int getDeckAgendaMinimum() {
        // Calculation: BASE_AGENDA + (floor(CardCount)/5*2)
        return (int) (BASE_AGENDA + (Math.floor(Math.max(getDeckSize(), getMinimumDeckSize()) / 5) * 2));
    }

    public void clearCardsToAddAndRemove() {
        mCardsToAdd.clear();
        mCardsToRemove.clear();
        mArrCardsToAdd.clear();
        mArrCardsToRemove.clear();
    }

    public void clearCardsToAddAndRemove(boolean onlyChecked) {
        if (!onlyChecked) {
            clearCardsToAddAndRemove();
        } else {
            // Cards to add
            Iterator<CardCount> it = mArrCardsToAdd.iterator();
            while (it.hasNext()) {
                CardCount cc = it.next();
                if (cc.isDone()) {
                    it.remove();
                    mCardsToAdd.remove(cc.getCard());
                }
            }
            // Cards to remove
            it = mArrCardsToRemove.iterator();
            while (it.hasNext()) {
                CardCount cc = it.next();
                if (cc.isDone()) {
                    it.remove();
                    mCardsToRemove.remove(cc.getCard());
                }
            }
        }
    }

    public boolean isInfluenceOk() {
        return (getDeckInfluence() <= getInfluenceLimit());
    }

    public boolean isCardCountOk() {
        return (getDeckSize() >= getMinimumDeckSize());
    }

    public boolean isAgendaOk() {
        return (getSide().equals(Card.Side.SIDE_RUNNER) || getDeckAgenda() == getDeckAgendaMinimum() || getDeckAgenda() == (getDeckAgendaMinimum() + 1));
    }

    public UUID getUUID() {
        return mUUID;
    }

    public Long getRowId() {
        return mRowId;
    }

    public void setRowId(Long newRowId) {
        mRowId = newRowId;
    }

    public void setCardsToAdd(ArrayList<CardCount> list) {
        mCardsToAdd.clear();
        for (CardCount cc : list) {
            mCardsToAdd.put(cc.getCard(), cc);
        }
        mArrCardsToAdd.clear();
        mArrCardsToAdd.addAll(list);
    }

    public void setCardsToRemove(ArrayList<CardCount> list) {
        mCardsToRemove.clear();
        for (CardCount cc : list) {
            mCardsToRemove.put(cc.getCard(), cc);
        }
        mArrCardsToRemove.clear();
        mArrCardsToRemove.addAll(list);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray jsonCards = new JSONArray();
        JSONArray jsonCardsToAdd = new JSONArray();
        JSONArray jsonCardsToRemove = new JSONArray();
        try {
            // Deck info
            json.putOpt(JSON_DECK_UUID, mUUID.toString());
            json.putOpt(JSON_DECK_NAME, mName);
            json.putOpt(JSON_DECK_NOTES, mNotes);
            json.putOpt(JSON_DECK_STARRED, mStarred);
            json.putOpt(JSON_DECK_IDENTITY_CODE, mIdentity.getCode());
            json.putOpt(JSON_DECK_FORMAT, this.format.getId());
            json.putOpt(JSON_DECK_PACK_FILTER, TextUtils.join(PACK_FILTER_SEPARATOR, packFilter));
            json.putOpt(JSON_DECK_CORE_COUNT, coreCount);

            // Cards
            ArrayList<Card> cardList = getCards();
            for (Card card : cardList) {
                JSONObject jsonCard = new JSONObject();
                jsonCard.putOpt(JSON_DECK_CARD_CODE, card.getCode());
                jsonCard.putOpt(JSON_DECK_CARD_COUNT, getCardCount(card));
                jsonCards.put(jsonCard);
            }

            // Cards to add
            for (CardCount cardCount : getCardsToAdd()) {
                JSONObject jsonCardCount = new JSONObject();
                jsonCardCount.putOpt(JSON_DECK_CARD_CODE, cardCount.getCard().getCode());
                jsonCardCount.putOpt(JSON_DECK_CARD_COUNT, cardCount.getCount());
                jsonCardCount.putOpt(JSON_DECK_CARDS_DONE, cardCount.isDone());
                jsonCardsToAdd.put(jsonCardCount);
            }

            // Cards to remove
            for (CardCount cardCount : getCardsToRemove()) {
                JSONObject jsonCardCount = new JSONObject();
                jsonCardCount.putOpt(JSON_DECK_CARD_CODE, cardCount.getCard().getCode());
                jsonCardCount.putOpt(JSON_DECK_CARD_COUNT, cardCount.getCount());
                jsonCardCount.putOpt(JSON_DECK_CARDS_DONE, cardCount.isDone());
                jsonCardsToRemove.put(jsonCardCount);
            }

            json.putOpt(JSON_DECK_CARDS, jsonCards);
            json.putOpt(JSON_DECK_CARDS_TO_ADD, jsonCardsToAdd);
            json.putOpt(JSON_DECK_CARDS_TO_REMOVE, jsonCardsToRemove);
        } catch (JSONException e) {
            //
            e.printStackTrace();
        }
        return json;
    }

    public static Deck fromJSON(JSONObject json, CardRepository repo) {
        String idCardCode = json.optString(JSON_DECK_IDENTITY_CODE);
        Card identityCard = repo.getCard(idCardCode);
        Format format = repo.getFormat(json.optInt(JSON_DECK_FORMAT));

        Deck deck = new Deck(identityCard, format);
        String packFilterValue = json.optString(JSON_DECK_PACK_FILTER);
        if (!packFilterValue.isEmpty()) {
            ArrayList<String> pf = new ArrayList<>(Arrays.asList(packFilterValue.split(PACK_FILTER_SEPARATOR)));
            deck.setPackFilter(pf);
        }
        deck.setCoreCount(json.optInt(JSON_DECK_CORE_COUNT, 0)); // specifying 0 to ensure meaning
        deck.mUUID = UUID.fromString(json.optString(JSON_DECK_UUID, UUID.randomUUID().toString()));
        deck.setName(json.optString(JSON_DECK_NAME));
        deck.setNotes(json.optString(JSON_DECK_NOTES));
        deck.setStarred(json.optBoolean(JSON_DECK_STARRED));

        // Get the cards
        try {
            JSONArray jsonCards = json.getJSONArray(JSON_DECK_CARDS);
            for (int i = 0; i < jsonCards.length(); i++) {
                JSONObject jsonCard = jsonCards.getJSONObject(i);
                deck.setCardCount(repo.getCard(jsonCard.optString(JSON_DECK_CARD_CODE)), jsonCard.optInt(JSON_DECK_CARD_COUNT));
            }

            // By default, when a new card is added to a deck, it is added to the ADD list
            deck.mCardsToAdd.clear();
            deck.mCardsToRemove.clear();
        } catch (JSONException e) {
        }

        // Get the cards to add
        try {
            JSONArray jsonCards = json.getJSONArray(JSON_DECK_CARDS_TO_ADD);
            for (int i = 0; i < jsonCards.length(); i++) {
                JSONObject jsonCard = jsonCards.getJSONObject(i);
                Card card = repo.getCard(jsonCard.optString(JSON_DECK_CARD_CODE));
                deck.mCardsToAdd.put(card, new CardCount(card, jsonCard.optInt(JSON_DECK_CARD_COUNT), jsonCard.optBoolean(JSON_DECK_CARDS_DONE)));
            }
        } catch (JSONException e) {
        }

        // Get the cards to remove
        try {
            JSONArray jsonCards = json.getJSONArray(JSON_DECK_CARDS_TO_REMOVE);
            for (int i = 0; i < jsonCards.length(); i++) {
                JSONObject jsonCard = jsonCards.getJSONObject(i);
                Card card = repo.getCard(jsonCard.optString(JSON_DECK_CARD_CODE));
                deck.mCardsToRemove.put(card, new CardCount(card, jsonCard.optInt(JSON_DECK_CARD_COUNT), jsonCard.optBoolean(JSON_DECK_CARDS_DONE)));
            }
        } catch (JSONException e) {
        }

        return deck;
    }

    public boolean isStarred() {
        return mStarred;
    }

    public void setStarred(boolean starred) {
        this.mStarred = starred;
    }

    public boolean isApex() {
        return getIdentity().getCode().equals(Card.SpecialCards.APEX);
    }

    public void setHasUnknownCards() {
        this.hasUnknownCards = true;
    }

    public boolean hasUnknownCards() {
        return hasUnknownCards;
    }

    public ArrayList<CardCount> getCardCounts() {
        ArrayList<CardCount> cardCounts = new ArrayList<>();
        for (Card card : mCards.keySet()) {
            Integer count = mCards.get(card);
            cardCounts.add(new CardCount(card, count));
        }
        return cardCounts;
    }

    public boolean isFaction(String factionCode) {
        return getIdentity().getFactionCode().equals(factionCode);
    }

    public void ReduceCard(Card card) {
        // not below 0
        int count = Math.max(0, getCardCount(card) - 1);

        updateCardCount(card, count);
    }

    public void AddCard(Card card, int maxAllowed) {
        // not above the maximum allowed
        int count = Math.min(maxAllowed, getCardCount(card) + 1);
        updateCardCount(card, count);
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public ArrayList<String> getPackFilter() {
        return packFilter;
    }

    public void setPackFilter(ArrayList<String> packFilter) {
        this.packFilter = packFilter;
    }

    public int getCoreCount() {
        return coreCount;
    }

    public void setCoreCount(int coreCount) {
        this.coreCount = coreCount;
    }

    public void replaceCard(Card card, Card replacement) {
        int count = this.mCards.get(card);
        updateCardCount(card, 0);
        updateCardCount(replacement, count);
    }
}
