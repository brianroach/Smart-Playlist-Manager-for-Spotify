package com.kwohlford.smartplaylistmanager.playlist.criteria;

/**
 * Type piece of a Criteria object, defines which data to apply the filter to.
 */
public enum CriteriaType {

    RATING("Rating"),
    GENRE("Genre tag"), MOOD("Mood tag"),
    ARTIST("Artist"), ALBUM("Album title");

    public final String display;
    CriteriaType(String display) {
        this.display = display;
    }

    public static ComparativeClause[] getValidComparators(CriteriaType type) {
        switch (type) {
            case RATING:
                return new ComparativeClause[] {
                        ComparativeClause.EQUALS,
                        ComparativeClause.NOT_EQ,
                        ComparativeClause.GREATER,
                        ComparativeClause.GREATER_EQ,
                        ComparativeClause.LESS,
                        ComparativeClause.LESS_EQ,
                        ComparativeClause.BETWEEN
                };
            case GENRE: case MOOD:
            case ARTIST: case ALBUM:
                return new ComparativeClause[] {
                        ComparativeClause.EQUALS,
                        ComparativeClause.NOT_EQ,
                        ComparativeClause.IN,
                        ComparativeClause.NOT_IN
                };
        }
        return new ComparativeClause[] {};
    }

    public static String[] getStringValues(CriteriaType[] types) {
        String[] strTypes = new String[types.length];
        for(int i = 0; i < types.length; i++) {
            strTypes[i] = types[i].display;
        }
        return strTypes;
    }

}
