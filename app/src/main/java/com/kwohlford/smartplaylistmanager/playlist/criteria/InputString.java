package com.kwohlford.smartplaylistmanager.playlist.criteria;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Criteria input containing one or more strings.
 */
public class InputString extends CriteriaInput<String> {

    public ArrayList<String> strings;

    @Override
    public void setInput(ArrayList<String> input) {
        strings = input;
    }

    @Override
    public String toString() {
        return strings.toString();
    }
}