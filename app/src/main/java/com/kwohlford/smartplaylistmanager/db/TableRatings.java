package com.kwohlford.smartplaylistmanager.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Table for storing user-defined track ratings.
 */
public class TableRatings extends DBTable {

    // Table name
    public static final String NAME = "ratings";

    // Column names
    public static final String COL_ID = "_id";
    public static final String COL_RATING = "rating";

    public TableRatings() {
        super(NAME);
    }

    @Override
    protected void generateDefaultData(SQLiteDatabase db) {
        // default is empty table
    }

    @Override
    public Map<String, DataType> getColumns() {
        if(columns != null) return columns;

        HashMap<String, DataType> cols = new HashMap<>();
        cols.put(COL_ID, DataType.TEXT_PK);
        cols.put(COL_RATING, DataType.REAL);
        return cols;
    }
}