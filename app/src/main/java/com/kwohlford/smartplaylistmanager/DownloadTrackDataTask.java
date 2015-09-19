package com.kwohlford.smartplaylistmanager;

import android.os.AsyncTask;
import android.widget.ProgressBar;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;

/**
 * Created by Kirsten on 19.09.2015.
 */
public class DownloadTrackDataTask extends AsyncTask<Pager<SavedTrack>, Void, TrackListing> {

    private ProgressBar progressBar;

    public DownloadTrackDataTask(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    protected TrackListing doInBackground(Pager<SavedTrack>... pagers) {
        Pager<SavedTrack> pager = pagers[0];

        ArrayList<TrackData> tracks;


        return null;
    }
}
