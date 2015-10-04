package com.kwohlford.smartplaylistmanager.playlist.criteria;

/**
 * Criteria set composed of a type, comparator, and value, used to generate SQL queries for making
 * playlists.
 */
public class Criteria {

    public boolean include;
    public CriteriaType type;
    public ComparativeClause comparator;
    public CriteriaInput inputtedValue;

    @Override
    public String toString() {
        return (include ? "Include" : "Exclude")
                    + " songs matching: "
                    + type.display + " "
                    + comparator.display + " "
                    + inputtedValue.toString();
    };

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

}
