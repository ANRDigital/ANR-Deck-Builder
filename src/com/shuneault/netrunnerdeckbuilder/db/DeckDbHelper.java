package com.shuneault.netrunnerdeckbuilder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

public class DeckDbHelper {
	
	/**
	 * SQL
	 */
	public static final String KEY_ID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_NOTES = "notes";
	public static final String KEY_IDENTITY_CODE = "identity_code";
	private static final String DATABASE_TABLE = "decks";
	private static final String DATABASE_CREATE = 
			"CREATE TABLE decks (" +
					"_id int primary key autoincrement, " +
					"name text not null ," +
					"notes text not null, " +
					"identity_code text not null" +
				");";
	
	private final Context mContext;
	
	/**
	 * Database stuff
	 */
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		private DatabaseHelper(Context context) {
			super(context, AppManager.DATABASE_NAME, null, AppManager.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Create the table
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Upgrade
			switch (oldVersion) {
				// db.execSQL("query"); ...
			}
		}
	}
	
	public DeckDbHelper(Context context) {
		mContext = context;
	}
	
	/**
	 * Open the decks database;
	 * 
	 * @return	this (self reference, allowing this to be chained in an
	 * 			initialization call)
	 */
	public DeckDbHelper open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	/**
	 * Create a new deck
	 * 
	 * @param	name	the name of the deck
	 * @param	notes	the notes of the deck
	 * @param	identity_code	the identity code of the deck
	 * @return	rowId or -1 if failed
	 */
	public Long createDeck(Deck deck) {
		ContentValues val = new ContentValues();
		val.put(KEY_NAME, deck.getName());
		val.put(KEY_NOTES, deck.getNotes());
		val.put(KEY_IDENTITY_CODE, deck.getIdentity().getCode());
		Long rowId = mDb.insert(DATABASE_TABLE, null, val);
		
		// Update the deck id
		deck.setRowId(rowId);
		
		return rowId;
	}
	
	public boolean deleteDeck(Long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0;
	}
	public boolean deleteDeck(Deck deck) {
		return deleteDeck(deck.getRowId());
	}
	
	public Cursor fetchAllDecks() {
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_NAME, KEY_NOTES, KEY_IDENTITY_CODE }, null, null, null, null, null);
	}
	
	public Cursor fetchDeck(Long rowId) throws SQLException {
		Cursor cursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ID, KEY_NAME, KEY_NOTES, KEY_IDENTITY_CODE }, KEY_ID + "=" + rowId, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public boolean updateDeck(Deck deck) {
		ContentValues val = new ContentValues();
		val.put(KEY_IDENTITY_CODE, deck.getIdentity().getCode());
		val.put(KEY_NAME, deck.getName());
		val.put(KEY_NOTES, deck.getNotes());
		
		return mDb.update(DATABASE_TABLE, val, KEY_ID + "=" + deck.getRowId(), null) > 0;
	}

}
