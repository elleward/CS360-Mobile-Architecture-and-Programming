package com.example.ellewardinventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper manages the SQLite database for user authentication and inventory storage.
 * This class handles creating tables, inserting data, querying data, updating records,
 * and deleting records for both users and inventory items.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database configuration
    private static final String DATABASE_NAME = "TagTalk.db";
    private static final int DATABASE_VERSION = 1;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Inventory table
    private static final String TABLE_INVENTORY = "inventory";
    private static final String COLUMN_ITEM_ID = "item_id";
    private static final String COLUMN_ITEM_NAME = "item_name";
    private static final String COLUMN_ITEM_QUANTITY = "quantity";
    private static final String COLUMN_ITEM_USER_ID = "user_id";

    /**
     * Constructor for DatabaseHelper
     * @param context The application context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * Creates the users and inventory tables.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUsersTable);

        // Create inventory table with foreign key to users
        String createInventoryTable = "CREATE TABLE " + TABLE_INVENTORY + " ("
                + COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL, "
                + COLUMN_ITEM_USER_ID + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + COLUMN_ITEM_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        db.execSQL(createInventoryTable);
    }

    /**
     * Called when the database needs to be upgraded.
     * Drops existing tables and recreates them.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ========================= USER OPERATIONS =========================

    /**
     * Adds a new user to the database.
     * @param username The username for the new account
     * @param password The password for the new account
     * @return The row ID of the newly inserted user, or -1 if an error occurred
     */
    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    /**
     * Checks if a username and password combination is valid.
     * @param username The username to check
     * @param password The password to check
     * @return true if credentials are valid, false otherwise
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE "
                + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * Checks if a username already exists in the database.
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * Gets the user ID for a given username.
     * @param username The username to look up
     * @return The user ID, or -1 if user not found
     */
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS
                + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    // ========================= INVENTORY OPERATIONS =========================

    /**
     * Adds a new inventory item to the database.
     * @param itemName The name of the item
     * @param quantity The quantity of the item
     * @param userId The ID of the user who owns this item
     * @return The row ID of the newly inserted item, or -1 if an error occurred
     */
    public long addInventoryItem(String itemName, int quantity, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_ITEM_QUANTITY, quantity);
        values.put(COLUMN_ITEM_USER_ID, userId);

        long result = db.insert(TABLE_INVENTORY, null, values);
        db.close();
        return result;
    }

    /**
     * Retrieves all inventory items for a specific user.
     * @param userId The ID of the user whose items to retrieve
     * @return A list of InventoryItem objects
     */
    public List<InventoryItem> getInventoryItems(int userId) {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_INVENTORY + " WHERE "
                + COLUMN_ITEM_USER_ID + " = ? ORDER BY " + COLUMN_ITEM_NAME;
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY));
                items.add(new InventoryItem(id, name, quantity));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }

    /**
     * Updates the quantity of an existing inventory item.
     * @param itemId The ID of the item to update
     * @param newQuantity The new quantity value
     * @return The number of rows affected (should be 1 if successful)
     */
    public int updateInventoryQuantity(int itemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_QUANTITY, newQuantity);

        int rowsAffected = db.update(TABLE_INVENTORY, values,
                COLUMN_ITEM_ID + " = ?", new String[]{String.valueOf(itemId)});
        db.close();
        return rowsAffected;
    }

    /**
     * Deletes an inventory item from the database.
     * @param itemId The ID of the item to delete
     * @return The number of rows deleted (should be 1 if successful)
     */
    public int deleteInventoryItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_INVENTORY,
                COLUMN_ITEM_ID + " = ?", new String[]{String.valueOf(itemId)});
        db.close();
        return rowsDeleted;
    }

    /**
     * Inner class representing an inventory item.
     * This class stores the essential data for each item in the inventory.
     */
    public static class InventoryItem {
        private int id;
        private String name;
        private int quantity;

        /**
         * Constructor for InventoryItem
         * @param id The unique identifier for this item
         * @param name The name of the item
         * @param quantity The quantity of the item in stock
         */
        public InventoryItem(int id, String name, int quantity) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }

        // Setters
        public void setId(int id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
