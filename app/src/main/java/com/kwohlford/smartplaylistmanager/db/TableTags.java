package com.kwohlford.smartplaylistmanager.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Table for storing available tags and related data.
 */
public class TableTags extends DBTable {

    // Table name
    public static final String NAME = "tags";

    // Column names
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_CATEGORY = "category";

    public TableTags() {
        super(NAME);
    }

    @Override
    protected void generateDefaultData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        // genre tags
        values.put(COL_NAME, "Indie");
        values.put(COL_CATEGORY, "1");
        db.insert(name, null, values);

        values.put(COL_NAME, "Electronic");
        db.insert(name, null, values);

        values.put(COL_NAME, "Rock");
        db.insert(name, null, values);

        // mood tags
        values.put(COL_NAME, "Chill");
        values.put(COL_CATEGORY, "2");
        db.insert(name, null, values);

        values.put(COL_NAME, "Rainy day");
        db.insert(name, null, values);

        values.put(COL_NAME, "Upbeat");
        db.insert(name, null, values);
    }

    @Override
    public Map<String, DataType> getColumns() {
        if(columns != null) return columns;

        HashMap<String, DataType> cols = new HashMap<>();
        cols.put(COL_ID, DataType.INTEGER_PK);
        cols.put(COL_NAME, DataType.TEXT);
        cols.put(COL_CATEGORY, DataType.INTEGER);
        return cols;
    }

}
