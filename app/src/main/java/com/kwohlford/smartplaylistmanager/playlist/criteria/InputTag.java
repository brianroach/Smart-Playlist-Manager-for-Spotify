package com.kwohlford.smartplaylistmanager.playlist.criteria;

import com.kwohlford.smartplaylistmanager.tracklist.Tag;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Criteria input containing one or more Tags.
 */
public class InputTag extends CriteriaInput<Tag> {

    public ArrayList<Tag> tags;

    @Override
    public void setInput(ArrayList<Tag> input) {
        tags = input;
    }

    @Override
    public String toString() {
        return Tag.tagListToString(tags);
    }
}
