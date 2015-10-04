package com.kwohlford.smartplaylistmanager.playlist.criteria;

import java.util.ArrayList;

/**
 * Container used to hold a user-defined input value, used in playlist-generation criteria.
 */
public abstract class CriteriaInput<T> {

    // todo: SQL generation
//    protected ComparativeClause comparator;
//    protected DBTable tableLocation;
//    protected String columnName;

//    public String getWhereClause() {
//        StringBuilder s = new StringBuilder()
//                .append(tableLocation.name)
//                .append(".")
//                .append(columnName)
//                .append(" ")
//                .append(comparator.sql)
//                .append(" ")
//                .append(getValueSql());
//        return s.toString();
//    }
//
//    protected abstract String getValueSql();

    public abstract void setInput(ArrayList<T> input);

    public abstract String toString();

}
