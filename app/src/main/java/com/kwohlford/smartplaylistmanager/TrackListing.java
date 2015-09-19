package com.kwohlford.smartplaylistmanager;

import java.util.ArrayList;

/**
 * Contains list of saved tracks.
 */
public class TrackListing {

    ArrayList<TrackData> savedTracks;

    public TrackListing(ArrayList<TrackData> savedTracks) {
        this.savedTracks = savedTracks;
    }

    public TrackData get(int index) {
        return savedTracks.get(index);
    }

    public int size() {
        return savedTracks.size();
    }

}
