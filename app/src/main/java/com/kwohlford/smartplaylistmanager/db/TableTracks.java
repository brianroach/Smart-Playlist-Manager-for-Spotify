package com.kwohlford.smartplaylistmanager.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table for storing user-defined track ratings.
 */
public class TableTracks extends DBTable {

    // Table name
    public static final String NAME = "tracks";

    // Column names
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "song_title";
    public static final String COL_ARTIST = "artist";
    public static final String COL_ALBUM = "album_title";
    public static final String COL_PREVIEW = "preview_url";
    public static final String COL_ALBUMART = "album_art_url";
    public static final String COL_RATING = "rating";

    public TableTracks() {
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
        cols.add(COL_TITLE);
        cols.add(COL_ARTIST);
        cols.add(COL_ALBUM);
        cols.add(COL_PREVIEW);
        cols.add(COL_ALBUMART);
        cols.add(COL_RATING);
        return cols;
    }

    @Override
    public Map<String, DataType> getColumnsTypes() {
        HashMap<String, DataType> cols = new HashMap<>();
        cols.put(COL_ID, DataType.TEXT_PK);
        cols.put(COL_TITLE, DataType.TEXT);
        cols.put(COL_ARTIST, DataType.TEXT);
        cols.put(COL_ALBUM, DataType.TEXT);
        cols.put(COL_PREVIEW, DataType.TEXT);
        cols.put(COL_ALBUMART, DataType.TEXT);
        cols.put(COL_RATING, DataType.REAL);
        return cols;
    }
}