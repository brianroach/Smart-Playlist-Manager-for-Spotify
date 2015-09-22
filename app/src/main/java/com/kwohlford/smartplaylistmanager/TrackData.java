package com.kwohlford.smartplaylistmanager;

import java.util.HashMap;

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
    public boolean cardExpanded = false;
    public boolean previewPlaying = false;


    /**
     * @param uri Spotify URI
     * @param songTitle Track title
     * @param artist Primary artist
     * @param albumName Album name
     * @param previewUrl URL to 30-sec preview clip
     * @param albumArtUrl URL to primary album art
     * @param rating User rating (out of 5)
     * @param genreTags Mapping of available genres to set value
     * @param moodTags Mapping of available moods to set value
     */
    public TrackData(
            String uri,
            String songTitle,
            String artist,
            String albumName,
            String previewUrl,
            String albumArtUrl,
            float rating,
            HashMap<Tag, Boolean> genreTags,
            HashMap<Tag, Boolean> moodTags) {
        this.uri = uri;
        this.songTitle = songTitle;
        this.artist = artist;
        this.albumName = albumName;
        this.previewUrl = previewUrl;
        this.albumArtUrl = albumArtUrl;
        this.rating = rating;
        this.genreTags = genreTags;
        this.moodTags = moodTags;
    }

    public void setRating(float rating) {
        this.rating = Math.max(Math.min(rating, 5f), 0f);
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

    /**
     * @param tag Tag to set value of
     * @param b True to tag this track, false to remove the tag
     */
    public void setTag(Tag tag, boolean b) {
        switch(tag.type) {
            case GENRE:
                genreTags.put(tag, b);
            case MOOD:
                moodTags.put(tag, b);
        }
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
