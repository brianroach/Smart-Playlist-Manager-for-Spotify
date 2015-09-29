package com.kwohlford.smartplaylistmanager.playlist;

/**
 * Created by Kirsten on 26.09.2015.
 */
public enum ComparativeClause {

    EQUALS("is"), NOT_EQ("is not"),
    GREATER("greater than"), GREATER_EQ("greater than or equal to"),
    LESS("less than"), LESS_EQ("less than or equal to"),
    BETWEEN("between"),
    IN("in"), NOT_IN("not in"),
    CONTAINS("contains");

    public final String display;
    ComparativeClause(String display) {
        this.display = display;
    }

    public static InputType getInputType(CriteriaType type, ComparativeClause comparator) {
        switch(type) {
            case RATING:
                switch(comparator) {
                    case EQUALS: case NOT_EQ:
                    case GREATER: case GREATER_EQ:
                    case LESS: case LESS_EQ:
                        return InputType.RATING_SINGLE;
                    case BETWEEN:
                        return InputType.RATING_DOUBLE;
                }
                break;
            case GENRE:
            case MOOD:
                switch(comparator) {
                    case EQUALS: case NOT_EQ:
                        return InputType.SPINNER_SINGLE;
                    case IN: case NOT_IN:
                        return InputType.SPINNER_MULTIPLE;
                }
                break;
            case ARTIST:
            case ALBUM:
                switch(comparator) {
                    case EQUALS: case NOT_EQ:
                        return InputType.SPINNER_SINGLE;
                    case IN: case NOT_IN:
                        return InputType.SPINNER_MULTIPLE;
                    case CONTAINS:
                        return InputType.FREETEXT;
                }
        }
        return InputType.NONE;
    }

    public static String[] getStringValues(ComparativeClause[] types) {
        String[] strTypes = new String[types.length];
        for(int i = 0; i < types.length; i++) {
            strTypes[i] = types[i].display;
        }
        return strTypes;
    }

}
