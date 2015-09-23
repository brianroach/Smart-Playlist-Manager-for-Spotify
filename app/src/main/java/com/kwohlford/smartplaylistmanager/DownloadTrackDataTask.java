package com.kwohlford.smartplaylistmanager;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.util.HashMap;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Downloads saved tracks and loads them into a RecyclerView.
 */
public class DownloadTrackDataTask extends AsyncTask<SpotifyService, Void, TrackListing> {

    private static final String TAG = "DownloadTrackData";
    private static final int TRACKS_PER_PAGE = 50;
    private static final String OPTIONS_LIMIT = "limit";
    private static final String OPTIONS_OFFSET = "offset";
    private final TrackListActivity trackListScreen;

    public DownloadTrackDataTask(TrackListActivity trackListScreen) {
        this.trackListScreen = trackListScreen;
    }

    @Override
    protected TrackListing doInBackground(SpotifyService... services) {
        SpotifyService spotify = services[0];
        HashMap<String,TrackData> tracks = new HashMap<>();

        int offset = 0;
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPTIONS_LIMIT, TRACKS_PER_PAGE);
        params.put(OPTIONS_OFFSET, offset);
        Pager<SavedTrack> savedTrackPager = spotify.getMySavedTracks(params);

//        tracks.putAll(extractTracksFromPage(savedTrackPager));

        while(savedTrackPager.next != null) {
            Log.d(TAG, "Retrieved " + savedTrackPager.items.size() + " saved tracks");
            tracks.putAll(extractTracksFromPage(savedTrackPager));
            offset += TRACKS_PER_PAGE;
            params.put(OPTIONS_OFFSET, offset);
            savedTrackPager = spotify.getMySavedTracks(params);
        }

        return new TrackListing(
                tracks,
                trackListScreen.database.getTagsByType(Tag.TagType.GENRE),
                trackListScreen.database.getTagsByType(Tag.TagType.MOOD));
    }

    protected void onPostExecute(TrackListing result) {
        TrackListAdapter mAdapter = new TrackListAdapter(result);
        trackListScreen.setRecyclerAdapter(mAdapter);
        trackListScreen.progressBar.setVisibility(View.GONE);
        trackListScreen.tracks = result;
    }

    /**
     * Extracts all necessary track data from a page of tracks.
     * @param page Current page
     * @return Mapping of track uris to their metadata
     */
    private HashMap<String,TrackData> extractTracksFromPage(Pager<SavedTrack> page) {
        HashMap<String,TrackData> trackMap = new HashMap<>();
        for(SavedTrack savedTrack : page.items) {
            Track track = savedTrack.track;

            trackMap.put(track.uri,
                    new TrackData(
                            track.uri,
                            track.name,
                            track.artists.get(0).name,
                            track.album.name,
                            track.preview_url,
                            track.album.images.get(0).url,
                            trackListScreen.database
                    )
            );
        }
        return trackMap;
    }

}
