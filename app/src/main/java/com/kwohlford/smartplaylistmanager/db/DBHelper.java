package com.kwohlford.smartplaylistmanager.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

/**
 * Database helper to accept queries and to manage db creation and updating.
 */
public class DBHelper extends SQLiteOpenHelper {

    private final List<DBTable> tables;

    public DBHelper(Context context, String dbName, int version, List<DBTable> tables) {
        super(context, dbName, null, version);
        this.tables = tables;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for(DBTable table : tables) {
            table.onCreate(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("Database", "Updating from version " + oldVersion + " to " + newVersion);
        dropAll(db);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("Database", "Downgrading from version " + oldVersion + " to " + newVersion);
        dropAll(db);
        onCreate(db);
    }

    private void dropAll(SQLiteDatabase db) {
        for(DBTable table : tables) {
            table.onUpgrade(db);
        }
    }
}
