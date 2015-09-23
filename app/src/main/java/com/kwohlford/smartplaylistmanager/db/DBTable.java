package com.kwohlford.smartplaylistmanager.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

/**
 * Model for a database table.
 */
public abstract class DBTable {

    public final String name;
    public final Map<String, DataType> columns;

    /* Possible types for column data */
    public enum DataType {
        INTEGER("INTEGER"),
        INTEGER_PK("INTEGER PRIMARY KEY AUTOINCREMENT"),
        TEXT("TEXT"),
        TEXT_PK("TEXT PRIMARY KEY"),
        REAL("REAL");
        public final String name;
        DataType(String name) { this.name = name; }
    }

    /**
     * @param name Name of this table
     */
    public DBTable(String name) {
        this.name = name;
        this.columns = getColumns();
    }

    /**
     * To be called on database creation.
     * @param db Writeable database
     */
    public void onCreate(SQLiteDatabase db) {
        if(columns.isEmpty()) return;

        StringBuilder s = new StringBuilder()
                .append("CREATE TABLE ")
                .append(name)
                .append("(");
        for(String colName : columns.keySet()) {
            s.append(colName)
                    .append(" ")
                    .append(columns.get(colName).name)
                    .append(", ");
        }
        s.replace(s.length()-2, s.length(), ")");

        db.execSQL(s.toString());
        generateDefaultData(db);
    }

    /**
     * To be called on database upgrade.
     * @param db Writeable database
     */
    public void onUpgrade(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + name);
    }

    /**
     * Called on table creation to insert default rows into the table.
     * @param db Writeable database
     */
    protected abstract void generateDefaultData(SQLiteDatabase db);

    /**
     * @return Mapping of columns to their data type
     */
    public abstract Map<String, DataType> getColumns();

}
