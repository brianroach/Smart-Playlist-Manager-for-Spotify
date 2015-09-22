package com.kwohlford.smartplaylistmanager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains list of saved tracks.
 */
public class TrackListing {

    private HashMap<String, TrackData> trackMap;
    private ArrayList<TrackData> savedTracks;
    public ArrayList<Tag> genreTags;
    public ArrayList<Tag> moodTags;

    public TrackListing(HashMap<String,TrackData> trackMap, ArrayList<Tag> genreTags, ArrayList<Tag> moodTags) {
        savedTracks = new ArrayList<>(trackMap.values());
        this.trackMap = trackMap;
        this.genreTags = genreTags;
        this.moodTags = moodTags;
    }

    public TrackData get(int index) {
        return savedTracks.get(index);
    }

    public int size() {
        return savedTracks.size();
    }

    public TrackData getTrackForUri(String uri) {
        return trackMap.get(uri);
    }

}
