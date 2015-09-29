package com.kwohlford.smartplaylistmanager.playlist;

import java.util.ArrayList;

/**
 * Created by Kirsten on 25.09.2015.
 */
public class Criteria {

    public boolean include;
    public CriteriaType type;
    public ComparativeClause comparator;

    public Criteria(boolean include, CriteriaType type, ComparativeClause comparator) {
        this.include = include;
        this.type = type;
        this.comparator = comparator;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder()
                .append(include ? "Include" : "Exclude")
                .append(" songs matching: ")
                .append(type.display)
                .append(" ")
                .append(comparator.display)
                .append(" (value)");
        return s.toString();
    }

}
