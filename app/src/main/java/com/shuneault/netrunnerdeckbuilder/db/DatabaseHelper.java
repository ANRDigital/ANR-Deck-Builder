package com.shuneault.netrunnerdeckbuilder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardCount;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;

import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * SQL
     */
    // Database Version
    private static final int DATABASE_VERSION = 7;

    // Database Name
    private static final String DATABASE_NAME = "deckBuilder.db";

    // Table Names
    private static final String TABLE_DECKS = "decks";
    private static final String TABLE_DECK_CARDS = "deck_cards";
    private static final String TABLE_DECK_CARDS_ADD = "deck_cards_add";
    private static final String TABLE_DECK_CARDS_REMOVE = "deck_cards_remove";

    // Common Column Names
    private static final String KEY_ID = "_id";

    // Table Decks
    private static final String KEY_DECKS_NAME = "name";
    private static final String KEY_DECKS_NOTES = "notes";
    private static final String KEY_DECKS_IDENTITY = "identity_code";
    private static final String KEY_DECKS_STARRED = "starred";
    private static final String KEY_DECKS_PACKFILTER = "pack_filter";
    public static final String PACK_FILTER_SEPARATOR = "~";
    private static final String KEY_DECKS_FORMAT = "format";
    private static final String KEY_DECKS_CORE_COUNT = "core_count";

    // Deck Cards
    private static final String KEY_DECK_CARDS_DECK_ID = "deck_id";
    private static final String KEY_DECK_CARDS_CODE = "card_code";
    private static final String KEY_DECK_CARDS_COUNT = "card_count";

    // Deck Cards To Add/Remove
    private static final String KEY_DECK_CARDS_ADD_REMOVE_DECK_ID = "deck_id";
    private static final String KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE = "card_code";
    private static final String KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT = "card_count";
    private static final String KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED = "is_checked";

    // Create Statements
    private static final String CREATE_TABLE_DECKS = "CREATE TABLE " + TABLE_DECKS + "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_DECKS_NAME + " TEXT NOT NULL," +
            KEY_DECKS_NOTES + " TEXT NOT NULL," +
            KEY_DECKS_IDENTITY + " TEXT NOT NULL" +
            ")";
    private static final String CREATE_TABLE_DECK_CARDS = "CREATE TABLE " + TABLE_DECK_CARDS + "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_DECK_CARDS_DECK_ID + " INTEGER NOT NULL," +
            KEY_DECK_CARDS_CODE + " TEXT NOT NULL," +
            KEY_DECK_CARDS_COUNT + " INTEGER NOT NULL" +
            ")";
    private static final String CREATE_TABLE_DECK_CARDS_ADD = "CREATE TABLE " + TABLE_DECK_CARDS_ADD + "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_DECK_CARDS_ADD_REMOVE_DECK_ID + " INTEGER NOT NULL," +
            KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE + " TEXT NOT NULL," +
            KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT + " INTEGER NOT NULL," +
            KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED + " INTEGER NOT NULL" +
            ")";
    private static final String CREATE_TABLE_DECK_CARDS_REMOVE = "CREATE TABLE " + TABLE_DECK_CARDS_REMOVE + "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_DECK_CARDS_ADD_REMOVE_DECK_ID + " INTEGER NOT NULL," +
            KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE + " TEXT NOT NULL," +
            KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT + " INTEGER NOT NULL," +
            KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED + " INTEGER NOT NULL" +
            ")";
    private static final String ALTER_TABLE_DECK_ADD_STARRED = "ALTER TABLE " + TABLE_DECKS + " " +
            "ADD " + KEY_DECKS_STARRED + " BIT NOT NULL DEFAULT(0)";

    private static final String ALTER_TABLE_DECK_ADD_PACKFILTER = "ALTER TABLE " + TABLE_DECKS + " " +
            "ADD " + KEY_DECKS_PACKFILTER + " TEXT NOT NULL DEFAULT('')";

    private static final String ALTER_TABLE_DECK_ADD_FORMAT = "ALTER TABLE " + TABLE_DECKS + " " +
            "ADD " + KEY_DECKS_FORMAT + " INTEGER";

    private static final String ALTER_TABLE_DECK_ADD_CORE_COUNT = "ALTER TABLE " + TABLE_DECKS + " " +
            "ADD " + KEY_DECKS_CORE_COUNT + " INTEGER";

    private static final String ALTER_TABLE_DECK_ADD_AUDIT_FIELDS = "ALTER TABLE " + TABLE_DECKS + " " +
            "ADD " + KEY_DECKS_CORE_COUNT + " INTEGER";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the required tables
        db.execSQL(CREATE_TABLE_DECKS);
        db.execSQL(CREATE_TABLE_DECK_CARDS);

        // Perform all upgrade
        onUpgrade(db, 1, DATABASE_VERSION);
    }

    // Upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                // Add 2 new tables
                db.execSQL(CREATE_TABLE_DECK_CARDS_ADD);
                db.execSQL(CREATE_TABLE_DECK_CARDS_REMOVE);
                // NO BREAK
            case 2:
                // Do some updates
            case 3:
                // Add "starred" field for decks
                db.execSQL(ALTER_TABLE_DECK_ADD_STARRED);
            case 4:
                // add pack filter field for decks
                db.execSQL(ALTER_TABLE_DECK_ADD_PACKFILTER);
            case 5:
                db.execSQL(ALTER_TABLE_DECK_ADD_FORMAT);
            case 6:
                db.execSQL(ALTER_TABLE_DECK_ADD_CORE_COUNT);
        }
    }


    /**
     * Create a new deck
     *
     * @param deck the deck to create
     * @return rowId or -1 if failed
     */
    public Long createDeck(Deck deck) {
        ContentValues val = new ContentValues();
        val.put(KEY_DECKS_NAME, deck.getName());
        val.put(KEY_DECKS_NOTES, deck.getNotes());
        val.put(KEY_DECKS_IDENTITY, deck.getIdentity().getCode());
        Long rowId = getWritableDatabase().insert(TABLE_DECKS, null, val);
        deck.setRowId(rowId);

        return rowId;
    }

    public boolean deleteDeck(Long rowId) {
        return getWritableDatabase().delete(TABLE_DECKS, KEY_ID + "=?", new String[]{String.valueOf(rowId)}) > 0;
    }

    public boolean deleteDeck(Deck deck) {
        return deleteDeck(deck.getRowId());
    }

    public boolean deleteAllDecks() {
        return getWritableDatabase().delete(TABLE_DECKS, null, null) > 0;
    }

    public ArrayList<Deck> getAllDecks(boolean withCards, CardRepository repo) {
        ArrayList<Deck> decks = new ArrayList<>();
        // read deck list from db
        Cursor c = getReadableDatabase().query(TABLE_DECKS,
                new String[]{KEY_ID, KEY_DECKS_NAME, KEY_DECKS_NOTES, KEY_DECKS_IDENTITY, KEY_DECKS_STARRED,
                        KEY_DECKS_PACKFILTER, KEY_DECKS_FORMAT, KEY_DECKS_CORE_COUNT},
                null, null, null, null, null);

        // create decks in list
        while (c.moveToNext()) {
            String deckName = c.getString(c.getColumnIndex(KEY_DECKS_NAME));
            String identityCode = c.getString(c.getColumnIndex(KEY_DECKS_IDENTITY));
            Card identity = repo.getCard(identityCode);

            int formatId = c.getInt(c.getColumnIndex(KEY_DECKS_FORMAT));
            Format format = repo.getFormat(formatId);
 //           pool = repo.getCardPool(format);
            Deck deck = new Deck(identity, format);
            if (identity.isUnknown())
            {
                deck.setHasUnknownCards(true);
            }
            deck.setName(deckName);
            deck.setNotes(c.getString(c.getColumnIndex(KEY_DECKS_NOTES)));
            deck.setRowId(c.getLong(c.getColumnIndex(KEY_ID)));
            deck.setStarred(c.getInt(c.getColumnIndex(KEY_DECKS_STARRED)) > 0);
            String packFilterValue = c.getString(c.getColumnIndex(KEY_DECKS_PACKFILTER));
//            CardPool pool;
            if (!packFilterValue.isEmpty()) {
                ArrayList<String> pf = new ArrayList<>(Arrays.asList(packFilterValue.split(PACK_FILTER_SEPARATOR)));
                deck.setPackFilter(pf);
            }
            deck.setCoreCount(c.getInt(c.getColumnIndex(KEY_DECKS_CORE_COUNT)));

            decks.add(deck);
        }

        // add cards to decks
        if (withCards) {
            for (Deck d : decks) {
                // Add cards to deck
                Long deckId = d.getRowId();
                try (Cursor c1 = getReadableDatabase().query(true,
                        TABLE_DECK_CARDS,
                        new String[]{KEY_DECK_CARDS_CODE, KEY_DECK_CARDS_COUNT},
                        KEY_DECK_CARDS_DECK_ID + "=?",
                        new String[]{String.valueOf(deckId)},
                        null, null, null, null)) {
                    while (c1.moveToNext()) {
                        String cardCode = c1.getString(c1.getColumnIndex(KEY_DECK_CARDS_CODE));
                        Card card = repo.getCard(cardCode);
                        if (card.isUnknown()) {
                            d.setHasUnknownCards(true);
                        }
                        int count = c1.getInt(c1.getColumnIndex(KEY_DECK_CARDS_COUNT));

                        d.setCardCount(card, count);
                    }
                }

                // Cards to add / remove
                d.setCardsToAdd(getDeckCardsToAdd(deckId, repo.getAllCards()));
                d.setCardsToRemove(getDeckCardsToRemove(deckId, repo.getAllCards()));
            }
        }
        return decks;
    }

    private ArrayList<CardCount> getDeckCardsToAdd(Long deckId, CardList allCards) {
        Cursor c = getReadableDatabase().query(true,
                TABLE_DECK_CARDS_ADD,
                new String[]{KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE, KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT, KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED},
                KEY_DECK_CARDS_ADD_REMOVE_DECK_ID + "=?",
                new String[]{String.valueOf(deckId)},
                null, null, null, null);

        return mapCardCounts(c, allCards);
    }

    private ArrayList<CardCount> getDeckCardsToRemove(Long deckId, CardList allCards) {
        Cursor c = getReadableDatabase().query(true,
                TABLE_DECK_CARDS_REMOVE,
                new String[]{KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE, KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT, KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED},
                KEY_DECK_CARDS_ADD_REMOVE_DECK_ID + "=?",
                new String[]{String.valueOf(deckId)},
                null, null, null, null);

        return mapCardCounts(c, allCards);
    }

    @NonNull
    private ArrayList<CardCount> mapCardCounts(Cursor c, CardList allCards) {
        ArrayList<CardCount> arrCards = new ArrayList<>();
        while (c.moveToNext()) {
            Card card = allCards.getCard(c.getString(c.getColumnIndex(KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE)));
            CardCount cc = new CardCount(card, c.getInt(c.getColumnIndex(KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT)), c.getInt(c.getColumnIndex(KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED)) == 1);
            arrCards.add(cc);
        }
        return arrCards;
    }

    public boolean clearCards(Deck deck) {
        return getWritableDatabase().delete(TABLE_DECK_CARDS,
                KEY_DECK_CARDS_DECK_ID + "=?",
                new String[]{deck.getRowId().toString()}) > 0;
    }

    public boolean clearCardsToAdd(Deck deck) {
        return getWritableDatabase().delete(TABLE_DECK_CARDS_ADD,
                KEY_DECK_CARDS_ADD_REMOVE_DECK_ID + "=?",
                new String[]{deck.getRowId().toString()}) > 0;
    }

    public boolean clearCardsToRemove(Deck deck) {
        return getWritableDatabase().delete(TABLE_DECK_CARDS_REMOVE,
                KEY_DECK_CARDS_ADD_REMOVE_DECK_ID + "=?",
                new String[]{deck.getRowId().toString()}) > 0;
    }

    public boolean updateCardCount(final Deck deck, final Card card, final int count) {
        SQLiteDatabase db = getWritableDatabase();

        // Delete the old card count
        db.delete(TABLE_DECK_CARDS,
                KEY_DECK_CARDS_DECK_ID + "=? AND " + KEY_DECK_CARDS_CODE + "=?",
                new String[]{deck.getRowId().toString(), card.getCode()}
        );

        // Add the new card count if necessary
        if (count > 0) {
            ContentValues val = new ContentValues();
            val.put(KEY_DECK_CARDS_DECK_ID, deck.getRowId());
            val.put(KEY_DECK_CARDS_COUNT, count);
            val.put(KEY_DECK_CARDS_CODE, card.getCode());
            db.insert(TABLE_DECK_CARDS, null, val);
        }
        return true;
    }

    /**
     * Updates the basic information of the deck (name, identity and notes). To save the whole deck, use saveDeck(Deck).
     *
     * @param deck
     * @return
     */
    public boolean updateDeck(Deck deck) {
        ContentValues val = new ContentValues();
        val.put(KEY_DECKS_IDENTITY, deck.getIdentity().getCode());
        val.put(KEY_DECKS_NAME, deck.getName());
        val.put(KEY_DECKS_NOTES, deck.getNotes());
        val.put(KEY_DECKS_STARRED, deck.isStarred());
        val.put(KEY_DECKS_PACKFILTER, TextUtils.join(PACK_FILTER_SEPARATOR, deck.getPackFilter()));
        val.put(KEY_DECKS_FORMAT, deck.getFormat().getId());
        val.put(KEY_DECKS_CORE_COUNT, deck.getCoreCount());

        return getWritableDatabase().update(TABLE_DECKS, val, KEY_ID + "=" + deck.getRowId(), null) > 0;
    }

    /**
     * Update the whole deck (name, identity, notes along with the cards in the deck).
     *
     * @param deck
     * @return
     */
    public boolean saveDeck(Deck deck) {
        // Create the deck if new
        if (deck.getRowId() == null) {
            deck.setRowId(createDeck(deck));
        }

        SQLiteDatabase db = getWritableDatabase();
        // Save the deck
        updateDeck(deck);

        // Save the cards
        clearCards(deck);
        for (Card card : deck.getCards()) {
            updateCardCount(deck, card, deck.getCardCount(card));
        }

        // Cards to add
        clearCardsToAdd(deck);
        for (CardCount cc : deck.getCardsToAdd()) {
            ContentValues valAdd = new ContentValues();
            valAdd.put(KEY_DECK_CARDS_ADD_REMOVE_DECK_ID, deck.getRowId());
            valAdd.put(KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE, cc.getCard().getCode());
            valAdd.put(KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT, cc.getCount());
            valAdd.put(KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED, cc.isDone());
            db.insert(TABLE_DECK_CARDS_ADD, null, valAdd);
        }

        // Cards to remove
        clearCardsToRemove(deck);
        for (CardCount cc : deck.getCardsToRemove()) {
            ContentValues valRemove = new ContentValues();
            valRemove.put(KEY_DECK_CARDS_ADD_REMOVE_DECK_ID, deck.getRowId());
            valRemove.put(KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE, cc.getCard().getCode());
            valRemove.put(KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT, cc.getCount());
            valRemove.put(KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED, cc.isDone());
            db.insert(TABLE_DECK_CARDS_REMOVE, null, valRemove);
        }

        return true;
    }

}
