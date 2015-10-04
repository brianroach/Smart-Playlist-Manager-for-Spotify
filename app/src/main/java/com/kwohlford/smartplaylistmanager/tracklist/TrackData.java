package com.kwohlford.smartplaylistmanager.tracklist;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

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
    public float rating;
    protected ArrayList<Tag> genreTags;
    protected ArrayList<Tag> moodTags;
    public boolean previewPlaying = false;

    /**
     * @param uri Spotify URI
     * @param songTitle Track title
     * @param artist Primary artist
     * @param albumName Album name
     * @param previewUrl URL to 30-sec preview clip
     * @param albumArtUrl URL to primary album art
     */
    public TrackData(
            String uri,
            String songTitle,
            String artist,
            String albumName,
            String previewUrl,
            String albumArtUrl) {
        this.uri = uri;
        this.songTitle = songTitle;
        this.artist = artist;
        this.albumName = albumName;
        this.previewUrl = previewUrl;
        this.albumArtUrl = albumArtUrl;
        this.rating = 0;
        this.genreTags = new ArrayList<>();
        this.moodTags = new ArrayList<>();
    }

    /**
     * @param type Category of tags to get
     * @return Mapping of tags to their set value
     */
    public ArrayList<Tag> getTags(Tag.TagType type) {
        switch(type) {
            case GENRE:
                return genreTags;
            case MOOD:
                return moodTags;
            default:
                return new ArrayList<>();
        }
    }

    /**
     * @param type Category of tags to set
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
     * @param type Category of tags to get
     * @return List of set tags as a string
     */
    public String getTagsAsString(Tag.TagType type) {
        StringBuilder s = new StringBuilder();
        ArrayList<Tag> tags;
        if(type == Tag.TagType.GENRE) {
            s.append("Genres: ");
            tags = genreTags;
        } else {
            s.append("Moods: ");
            tags = moodTags;
        }
        s.append(Tag.tagListToString(tags));
        return s.toString();
    }

}
