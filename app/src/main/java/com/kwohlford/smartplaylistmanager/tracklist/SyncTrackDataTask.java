package com.kwohlford.smartplaylistmanager.tracklist;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.kwohlford.smartplaylistmanager.StartActivity;
import com.kwohlford.smartplaylistmanager.db.SourceTrackData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Syncs saved tracks with data loaded from spotify API.
 */
public class SyncTrackDataTask extends AsyncTask<SpotifyService, String, Void> {

    private static final String TAG = "DownloadTrackData";
    private static final int TRACKS_PER_PAGE = 50;
    private static final String OPTIONS_LIMIT = "limit";
    private static final String OPTIONS_OFFSET = "offset";

    private static final int NUM_STEPS = 3;

    private final ProgressDialog progress;
    private boolean successful = true;

    public SyncTrackDataTask(ProgressDialog progress) {
        this.progress = progress;
    }

    @Override
    protected void onPreExecute() {
        progress.setTitle("Syncing with Spotify");
        progress.setMessage("Connecting to Spotify server...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setProgress(0);
        progress.setMax(NUM_STEPS);
        progress.setButton(DialogInterface.BUTTON_POSITIVE, "Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progress.dismiss();
            }
        });
        progress.show();
        progress.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(SpotifyService... services) {
        publishProgress("Step 1 of 3: Downloading track data");
        TrackListing downloadedTracks = downloadTracks(services[0]);
        if(!successful) { return null; }
        TrackListing cachedTracks = SourceTrackData.getInstance().tracks;
        Log.d(TAG, "Loaded " + cachedTracks.size() + " cached tracks");

        publishProgress("Step 2 of 3: Syncing tracks");
        syncTracks(downloadedTracks, cachedTracks);

        publishProgress("Step 3 of 3: Updating playlists");
        // If the deleted tracks were in any playlists, update them
        // playlists not yet implemented

        publishProgress("Completing sync...");
        return null;
    }

    @Override
    protected void onProgressUpdate(String... messages) {
        progress.incrementProgressBy(1);
        progress.setMessage(messages[0]);
    }

    @Override
    protected void onPostExecute(Void result) {
        if(successful) {
            progress.setTitle("Sync successful");
            progress.setMessage("Sync completed!");
        } else {
            progress.setTitle("Sync failed");
            progress.setMessage("Unable to connect to spotify servers. " +
                    "Check your internet connection or try again later.");
        }
        progress.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
    }

    private void syncTracks(TrackListing downloadedTracks, TrackListing cachedTracks) {
        SourceTrackData dataSource = SourceTrackData.getInstance();

        // Add new tracks
        Set<String> newTracks = new HashSet<>(downloadedTracks.getAllTrackUris());
        Set<String> oldTracks = new HashSet<>(cachedTracks.getAllTrackUris());
        newTracks.removeAll(oldTracks);
        Log.d(TAG, "Found " + newTracks.size() + " new tracks");
        for(String trackUri : newTracks) {
            dataSource.addTrack(downloadedTracks.getTrackForUri(trackUri));
        }

        // Remove deleted tracks
        newTracks = new HashSet<>(downloadedTracks.getAllTrackUris());
        oldTracks = new HashSet<>(cachedTracks.getAllTrackUris());
        oldTracks.removeAll(newTracks);
        Log.d(TAG, "Found " + oldTracks.size() + " tracks to delete");
        for(String trackUri : oldTracks) {
            dataSource.deleteTrack(cachedTracks.getTrackForUri(trackUri));
        }
    }

    private TrackListing downloadTracks(SpotifyService spotify) {
        HashMap<String,TrackData> tracks = new HashMap<>();

        int offset = 0;
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPTIONS_LIMIT, TRACKS_PER_PAGE);
        params.put(OPTIONS_OFFSET, offset);
        try {
            Pager<SavedTrack> savedTrackPager = spotify.getMySavedTracks(params);

            while(savedTrackPager.next != null) {
                Log.d(TAG, "Retrieved " + savedTrackPager.items.size() + " saved tracks");
                tracks.putAll(extractTracksFromPage(savedTrackPager));
                offset += TRACKS_PER_PAGE;
                params.put(OPTIONS_OFFSET, offset);
                savedTrackPager = spotify.getMySavedTracks(params);
            }
            tracks.putAll(extractTracksFromPage(savedTrackPager));

            Log.d(TAG, "Finished downloading " + tracks.size() + " tracks");
            return new TrackListing(tracks);
        } catch (Exception e) {
            Log.d(TAG, "Failed downloading tracks: " + e.getMessage());
            successful = false;
            return null;
        }
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
                            track.album.images.get(0).url
                    )
            );
        }
        return trackMap;
    }

}
