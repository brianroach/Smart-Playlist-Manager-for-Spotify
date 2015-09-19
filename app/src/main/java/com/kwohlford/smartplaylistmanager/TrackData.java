package com.kwohlford.smartplaylistmanager;

/**
 * Created by Kirsten on 18.09.2015.
 */
public class TrackData {
    String songTitle;
    String artist;
    String albumName;
    String previewUrl;
    String albumArtUrl;

    public TrackData(String songTitle, String artist, String albumName, String previewUrl, String albumArtUrl) {
        this.songTitle = songTitle;
        this.artist = artist;
        this.albumName = albumName;
        this.previewUrl = previewUrl;
        this.albumArtUrl = albumArtUrl;
    }
}
