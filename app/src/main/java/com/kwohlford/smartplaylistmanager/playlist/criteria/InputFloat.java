package com.kwohlford.smartplaylistmanager.playlist.criteria;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Criteria input containing one or two floats.
 */
public class InputFloat extends CriteriaInput<Float> {

    private boolean hasMultiple;
    public float digitMin;
    public float digitMax;

    @Override
    public void setInput(ArrayList<Float> input) {
        if(input.size() == 1) {
            hasMultiple = false;
            digitMin = input.get(0);
        } else if(input.size() == 2) {
            hasMultiple = true;
            digitMin = Math.min(input.get(0), input.get(1));
            digitMax = Math.max(input.get(0), input.get(1));
        }
    }

    @Override
    public String toString() {
        return hasMultiple ? (digitMin + " and " + digitMax) : String.valueOf(digitMin);
    }
}
