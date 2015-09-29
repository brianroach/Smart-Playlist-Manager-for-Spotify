package com.kwohlford.smartplaylistmanager.tracklist;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains list of saved tracks.
 */
public class TrackListing {

    private final HashMap<String, TrackData> trackMap;
    private final ArrayList<TrackData> savedTracks;
    private ArrayList<Tag> genreTags;
    private ArrayList<Tag> moodTags;

    /**
     * Create a new track list.
     * @param trackMap Mapping of track uris to their data
     */
    public TrackListing(HashMap<String,TrackData> trackMap) {
        savedTracks = new ArrayList<>(trackMap.values());
        this.trackMap = trackMap;
        this.genreTags = new ArrayList<>();
        this.moodTags = new ArrayList<>();
    }

    /**
     * Gets the track at the given index (needed for recyclerview adapter).
     * @param index Index
     * @return Track at index
     */
    public TrackData get(int index) {
        return savedTracks.get(index);
    }

    /**
     * @return Number of tracks in the dataset
     */
    public int size() {
        return savedTracks.size();
    }

    /**
     * Looks up the track data for a track uri.
     * @param uri Uri of track to lookup
     * @return Track data
     */
    public TrackData getTrackForUri(String uri) {
        return trackMap.get(uri);
    }

    /**
     * @param type Type of tags to get
     * @return List of tags
     */
    public ArrayList<Tag> getTags(Tag.TagType type) {
        switch (type) {
            case GENRE:
                return genreTags;
            case MOOD:
                return moodTags;
            default:
                return new ArrayList<>();
        }
    }

    /**
     * @param type Type of tags to set
     * @param tags List of tags
     */
    public void setTags(Tag.TagType type, ArrayList<Tag> tags) {
        switch(type) {
            case GENRE:
                genreTags = tags;
            case MOOD:
                moodTags = tags;
        }
    }

    /**
     * Removes a tag from the list of available tags.
     * @param t Tag to remove
     */
    public void deleteTag(Tag t) {
        getTags(t.type).remove(t);
    }

    /**
     * Adds a tag from the list of available tags.
     * @param t Tag to add
     */
    public void addTag(Tag t) {
        getTags(t.type).add(t);
    }

    public ArrayList<String> getAllTrackUris() {
        return new ArrayList<>(trackMap.keySet());
    }

    public void addTrack(TrackData track) {
        trackMap.put(track.uri, track);
        savedTracks.add(track);
    }

    public void removeTrack(TrackData track) {
        trackMap.remove(track.uri);
        savedTracks.remove(track);
    }

}
