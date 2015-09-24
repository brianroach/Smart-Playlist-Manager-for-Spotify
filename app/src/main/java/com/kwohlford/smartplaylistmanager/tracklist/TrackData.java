package com.kwohlford.smartplaylistmanager.tracklist;

import com.kwohlford.smartplaylistmanager.db.SourceTrackData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Container for storing data for a single track.
 */
public class TrackData {

    public final String uri;
    public final String songTitle;
    public final String artist;
    public final String albumName;
    public final String previewUrl;
    public final String albumArtUrl;
    private float rating;
    protected HashMap<Tag, Boolean> genreTags;
    protected HashMap<Tag, Boolean> moodTags;
    public boolean previewPlaying = false;
    public final SourceTrackData database;

    /**
     * @param uri Spotify URI
     * @param songTitle Track title
     * @param artist Primary artist
     * @param albumName Album name
     * @param previewUrl URL to 30-sec preview clip
     * @param albumArtUrl URL to primary album art
     * @param database Data source for saving track changes
     */
    public TrackData(
            String uri,
            String songTitle,
            String artist,
            String albumName,
            String previewUrl,
            String albumArtUrl,
            SourceTrackData database) {
        this.uri = uri;
        this.songTitle = songTitle;
        this.artist = artist;
        this.albumName = albumName;
        this.previewUrl = previewUrl;
        this.albumArtUrl = albumArtUrl;
        this.database = database;
        rating = database.getRating(uri);
        genreTags = database.getTrackTags(uri, Tag.TagType.GENRE);
        moodTags = database.getTrackTags(uri, Tag.TagType.MOOD);
    }

    public void setRating(float rating) {
        this.rating = rating;
        database.setRating(uri, rating);
    }

    public float getRating() {
        return rating;
    }

    /**
     * @param type Category of tags to get
     * @return Mapping of tags to their set value
     */
    public HashMap<Tag, Boolean> getTags(Tag.TagType type) {
        switch(type) {
            case GENRE:
                return genreTags;
            case MOOD:
                return moodTags;
            default:
                return new HashMap<>();
        }
    }

    public void setTags(Tag.TagType type, HashMap<Tag, Boolean> tagMapping) {
        HashMap<Tag, Boolean> oldMap;
        switch(type) {
            case GENRE:
                oldMap = genreTags;
                genreTags = tagMapping;
                break;
            case MOOD:
                oldMap = moodTags;
                moodTags = tagMapping;
                break;
            default:
                oldMap = new HashMap<>();
        }

        Set<Map.Entry<Tag, Boolean>> changedEntries = new HashSet<>(tagMapping.entrySet());
        changedEntries.removeAll(oldMap.entrySet());

        database.setTrackTags(uri, changedEntries);
    }

    /**
     * @param type Category of tags to get
     * @return List of set tags as a string
     */
    public String getTagsAsString(Tag.TagType type) {
        StringBuilder s = new StringBuilder();
        HashMap<Tag, Boolean> tags;
        if(type == Tag.TagType.GENRE) {
            s.append("Genres: ");
            tags = genreTags;
        } else {
            s.append("Moods: ");
            tags = moodTags;
        }

        boolean hasTags = false;
        for(Tag tag : tags.keySet()) {
            if(tags.get(tag)) {
                hasTags = true;
                s.append(tag).append(", ");
            }
        }
        if(hasTags) {
            s.deleteCharAt(s.lastIndexOf(","));
        } else {
            s.append("none");
        }
        return s.toString();
    }

}
