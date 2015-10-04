package com.kwohlford.smartplaylistmanager.playlist.criteria;

/**
 * Comparator piece of a Criteria object, defines relation between criteria type and inputted value.
 */
public enum ComparativeClause {

    EQUALS("is", "IS"), NOT_EQ("is not", "IS NOT"),
    GREATER("greater than", ">"), GREATER_EQ("greater than or equal to", ">="),
    LESS("less than", "<"), LESS_EQ("less than or equal to", "<="),
    BETWEEN("between", "BETWEEN"),
    IN("in", "IN"), NOT_IN("not in", "NOT IN");

    public final String display;
    public final String sql;
    ComparativeClause(String display, String sql) {
        this.display = display;
        this.sql = sql;
    }

    public static String[] getStringValues(ComparativeClause[] types) {
        String[] strTypes = new String[types.length];
        for(int i = 0; i < types.length; i++) {
            strTypes[i] = types[i].display;
        }
        return strTypes;
    }

}
