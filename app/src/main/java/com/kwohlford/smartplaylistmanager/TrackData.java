package com.kwohlford.smartplaylistmanager;

/**
 * Container for storing data for a single track.
 */
public class TrackData {

    public final String songTitle;
    public final String artist;
    public final String albumName;
    public final String previewUrl;
    public final String albumArtUrl;

    /**
     * @param songTitle Track title
     * @param artist Primary artist
     * @param albumName Album name
     * @param previewUrl URL to 30-sec preview clip
     * @param albumArtUrl URL to primary album art
     */
    public TrackData(String songTitle, String artist, String albumName, String previewUrl, String albumArtUrl) {
        this.songTitle = songTitle;
        this.artist = artist;
        this.albumName = albumName;
        this.previewUrl = previewUrl;
        this.albumArtUrl = albumArtUrl;
    }
}
