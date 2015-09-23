package com.kwohlford.smartplaylistmanager.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Access point to database, manages creation and lifecycle. Meant to be subclassed to provide
 * convenience methods for commonly used queries.
 */
public class DBDataSource {

    public final String dbName;
    public final int dbVersion;

    protected SQLiteDatabase database;
    protected DBHelper dbHelper;

    public DBDataSource(Context context, String dbName, int dbVersion, DBTable... tables) {
        dbHelper = new DBHelper(
                context, dbName, dbVersion,
                new ArrayList<>(Arrays.asList(tables))
        );
        this.dbName = dbName;
        this.dbVersion = dbVersion;
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

}
