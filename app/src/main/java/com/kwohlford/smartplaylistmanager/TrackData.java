package com.kwohlford.smartplaylistmanager;

import java.util.ArrayList;

/**
 * Container for storing data for a single track.
 */
public class TrackData {

    public final String songTitle;
    public final String artist;
    public final String albumName;
    public final String previewUrl;
    public final String albumArtUrl;
    private int rating;
    private ArrayList<Tag> tags;
    public boolean cardExpanded = false;

    /**
     * @param songTitle Track title
     * @param artist Primary artist
     * @param albumName Album name
     * @param previewUrl URL to 30-sec preview clip
     * @param albumArtUrl URL to primary album art
     */
    public TrackData(
            String songTitle,
            String artist,
            String albumName,
            String previewUrl,
            String albumArtUrl,
            int rating,
            ArrayList<Tag> tags) {
        this.songTitle = songTitle;
        this.artist = artist;
        this.albumName = albumName;
        this.previewUrl = previewUrl;
        this.albumArtUrl = albumArtUrl;
        this.rating = rating;
        this.tags = tags;
    }

    public void setRating(int rating) {
        this.rating = Math.max(Math.min(rating, 5), 0);
    }

    public int getRating() {
        return rating;
    }

    public String getTagsAsString() {
        if(tags.isEmpty()) return "No tags set.";

        StringBuilder s = new StringBuilder();
        for(Tag tag : tags) {
            s.append(tag).append(", ");
        }
        s.deleteCharAt(s.length()-1);
        s.deleteCharAt(s.length()-1);
        return s.toString();
    }
}
