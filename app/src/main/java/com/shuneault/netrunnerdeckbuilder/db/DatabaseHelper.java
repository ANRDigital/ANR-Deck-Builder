package com.shuneault.netrunnerdeckbuilder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardCount;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * SQL
     */
    // Database Version
    private static final int DATABASE_VERSION = 4;

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
        }
    }


    /**
     * Create a new deck
     *
     * @param    deck    the deck to create
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

    public ArrayList<Deck> getAllDecks(boolean withCards) {
        ArrayList<Deck> decks = new ArrayList<Deck>();
        Cursor c = getReadableDatabase().query(TABLE_DECKS, new String[]{KEY_ID, KEY_DECKS_NAME, KEY_DECKS_NOTES, KEY_DECKS_IDENTITY, KEY_DECKS_STARRED}, null, null, null, null, null);

        // Loop and create objects as we go
        if (c.moveToFirst()) {
            do {
                Deck deck = new Deck(c.getString(c.getColumnIndex(KEY_DECKS_NAME)), c.getString(c.getColumnIndex(KEY_DECKS_IDENTITY)));
                deck.setNotes(c.getString(c.getColumnIndex(KEY_DECKS_NOTES)));
                deck.setRowId(c.getLong(c.getColumnIndex(KEY_ID)));
                deck.setStarred(c.getInt(c.getColumnIndex(KEY_DECKS_STARRED)) > 0);

                // Add the cards?
                if (withCards) {
                    // Add cards
                    for (CardCount cc : getDeckCards(deck.getRowId())) {
                        deck.setCardCount(cc.getCard(), cc.getCount());
                    }
                    // Cards to add
                    deck.setCardsToAdd(getDeckCardsToAdd(deck.getRowId()));

                    // Cards to remove
                    deck.setCardsToRemove(getDeckCardsToRemove(deck.getRowId()));
                }

                decks.add(deck);
            } while (c.moveToNext());
        }
        return decks;
    }

    public ArrayList<CardCount> getDeckCards(Long deckId) {
        ArrayList<CardCount> arrCards = new ArrayList<CardCount>();
        Cursor c = getReadableDatabase().query(true,
                TABLE_DECK_CARDS,
                new String[]{KEY_DECK_CARDS_CODE, KEY_DECK_CARDS_COUNT},
                KEY_DECK_CARDS_DECK_ID + "=?",
                new String[]{String.valueOf(deckId)},
                null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Card card = AppManager.getInstance().getCard(c.getString(c.getColumnIndex(KEY_DECK_CARDS_CODE)));
                    CardCount cc = new CardCount(card, c.getInt(c.getColumnIndex(KEY_DECK_CARDS_COUNT)));
                    arrCards.add(cc);
                } while (c.moveToNext());
            }
        }
        return arrCards;
    }

    public ArrayList<CardCount> getDeckCardsToAdd(Long deckId) {
        ArrayList<CardCount> arrCards = new ArrayList<CardCount>();
        Cursor c = getReadableDatabase().query(true,
                TABLE_DECK_CARDS_ADD,
                new String[]{KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE, KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT, KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED},
                KEY_DECK_CARDS_ADD_REMOVE_DECK_ID + "=?",
                new String[]{String.valueOf(deckId)},
                null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Card card = AppManager.getInstance().getCard(c.getString(c.getColumnIndex(KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE)));
                    CardCount cc = new CardCount(card, c.getInt(c.getColumnIndex(KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT)), c.getInt(c.getColumnIndex(KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED)) == 1);
                    arrCards.add(cc);
                } while (c.moveToNext());
            }
        }
        return arrCards;
    }

    public ArrayList<CardCount> getDeckCardsToRemove(Long deckId) {
        ArrayList<CardCount> arrCards = new ArrayList<CardCount>();
        Cursor c = getReadableDatabase().query(true,
                TABLE_DECK_CARDS_REMOVE,
                new String[]{KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE, KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT, KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED},
                KEY_DECK_CARDS_ADD_REMOVE_DECK_ID + "=?",
                new String[]{String.valueOf(deckId)},
                null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Card card = AppManager.getInstance().getCard(c.getString(c.getColumnIndex(KEY_DECK_CARDS_ADD_REMOVE_CARD_CODE)));
                    CardCount cc = new CardCount(card, c.getInt(c.getColumnIndex(KEY_DECK_CARDS_ADD_REMOVE_CARD_COUNT)), c.getInt(c.getColumnIndex(KEY_DECK_CARDS_ADD_REMOVE_CARD_CHECKED)) == 1);
                    arrCards.add(cc);
                } while (c.moveToNext());
            }
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
