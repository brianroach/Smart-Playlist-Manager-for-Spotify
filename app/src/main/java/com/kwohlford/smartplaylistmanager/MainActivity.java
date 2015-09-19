package com.kwohlford.smartplaylistmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    private Config config;


    private Player mPlayer;


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        config = Config.loadConfig(this);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(config.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                config.REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "user-library-read"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, config.REQUEST_CODE, request);


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_track_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

//        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
//        progressBar.setVisibility(View.VISIBLE);

//        TrackData data = new TrackData("test", "test", "test", R.drawable.album_placeholder);
//        ArrayList<TrackData> tracksList = new ArrayList<TrackData>();
//        tracksList.add(data);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == config.REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {


                // START PREVIEW TRACK PLAYBACK
//                try {
//                    MediaPlayer player = new MediaPlayer();
//                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    player.setDataSource("https://p.scdn.co/mp3-preview/01bb2a6c9a89c05a4300aea427241b1719a26b06");
//                    player.prepare();
//                    player.start();
//                } catch (Exception e) {
//                }
                // END PREVIEW TRACK PLAYBACK



                // START GET TRACK DATA
                SpotifyApi api = new SpotifyApi();
                api.setAccessToken(response.getAccessToken());
                SpotifyService spotify = api.getService();

                spotify.getMySavedTracks(new Callback<Pager<SavedTrack>>() {
                    @Override
                    public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                        Log.d("Retrieved saved tracks", String.valueOf(savedTrackPager.items.size()));
                        List<SavedTrack> tracks = savedTrackPager.items;
                        SavedTrack firstTrack = tracks.get(0);
                        Track track = firstTrack.track;
                        List<ArtistSimple> artists = track.artists;
                        List<Image> albumArtList = track.album.images;
                        Image albumArt = albumArtList.get(0);
                        TrackData data = new TrackData(track.name, artists.get(0).name, track.album.name, track.preview_url, albumArt.url);

                        ArrayList<TrackData> tracksList = new ArrayList<TrackData>();
                        tracksList.add(data);
                        mAdapter = new TrackListAdapter(tracksList);
                        mRecyclerView.setAdapter(mAdapter);

//                        TextView txtBox = (TextView) findViewById(R.id.txt1);
//                        txtBox.setText(track.name);

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Failure getting tracks", error.toString());
                    }
                });
                // END GET TRACK DATA

            }
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(PlayerNotificationCallback.ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
