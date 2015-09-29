package com.kwohlford.smartplaylistmanager.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table for storing tags associated with tracks.
 */
public class TableTrackTags extends DBTable {

    // Table name
    public static final String NAME = "track_tags";

    // Column names
    public static final String COL_ID = "_id";
    public static final String COL_TRACKID = "track_id";
    public static final String COL_TAGID = "tag_id";

    public TableTrackTags() {
        super(NAME);
    }

    @Override
    protected void generateDefaultData(SQLiteDatabase db) {
        // default is empty table
    }

    @Override
    public List<String> getColumns() {
        if(columns != null) return columns;

        ArrayList<String> cols = new ArrayList<>();
        cols.add(COL_ID);
        cols.add(COL_TRACKID);
        cols.add(COL_TAGID);
        return cols;
    }

    @Override
    public Map<String, DataType> getColumnsTypes() {
        HashMap<String, DataType> cols = new HashMap<>();
        cols.put(COL_ID, DataType.INTEGER_PK);
        cols.put(COL_TRACKID, DataType.TEXT);
        cols.put(COL_TAGID, DataType.INTEGER);
        return cols;
    }
}