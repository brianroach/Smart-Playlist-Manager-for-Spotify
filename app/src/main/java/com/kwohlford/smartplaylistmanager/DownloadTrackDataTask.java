package com.kwohlford.smartplaylistmanager;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
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
    private RecyclerView recycler;
    private ProgressBar progressBar;

    public DownloadTrackDataTask(RecyclerView recycler, ProgressBar progressBar) {
        this.progressBar = progressBar;
        this.recycler = recycler;
    }

    @Override
    protected TrackListing doInBackground(SpotifyService... services) {
        SpotifyService spotify = services[0];
        ArrayList<TrackData> tracks = new ArrayList<>();

        int offset = 0;
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPTIONS_LIMIT, TRACKS_PER_PAGE);
        params.put(OPTIONS_OFFSET, offset);
        Pager<SavedTrack> savedTrackPager = spotify.getMySavedTracks(params);

//        tracks.addAll(extractTracksFromPage(savedTrackPager));

        while(savedTrackPager.next != null) {
            Log.d(TAG, "Retrieved " + savedTrackPager.items.size() + " saved tracks");
            tracks.addAll(extractTracksFromPage(savedTrackPager));
            offset += TRACKS_PER_PAGE;
            params.put(OPTIONS_OFFSET, offset);
            savedTrackPager = spotify.getMySavedTracks(params);
        }

        return new TrackListing(tracks);
    }

    protected void onPostExecute(TrackListing result) {
        RecyclerView.Adapter mAdapter = new TrackListAdapter(result);
        recycler.setAdapter(mAdapter);
        progressBar.setVisibility(View.GONE);
    }

    private ArrayList<TrackData> extractTracksFromPage(Pager<SavedTrack> page) {
        ArrayList<TrackData> tracks = new ArrayList<>();
        for(SavedTrack savedTrack : page.items) {
            Track track = savedTrack.track;
            tracks.add(
                    new TrackData(
                            track.name,
                            track.artists.get(0).name,
                            track.album.name,
                            track.preview_url,
                            track.album.images.get(0).url
                    )
            );
        }
        return tracks;
    }

}
