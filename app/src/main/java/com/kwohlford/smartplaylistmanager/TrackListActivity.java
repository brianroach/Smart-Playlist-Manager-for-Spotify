package com.kwohlford.smartplaylistmanager;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Screen for displaying list of user's saved tracks.
 */
public class TrackListActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // Configuration loader
    private Config config;

    // Layout & view managers
    private RecyclerView recycler;
    private RecyclerView.LayoutManager recyclerLayoutManager;

    // Loading bar
    private ProgressBar progressBar;

    // Vars for tracking playback of preview clips
    private boolean playback;
    private ImageView playbackButton;
    private MediaPlayer player;
    private String playbackTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load API authentication data from config file
        config = Config.loadConfig(this);

        // Prompt user login
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(config.clientId,
                AuthenticationResponse.Type.TOKEN,
                config.redirectUri);
        builder.setScopes(new String[]{"user-read-private", "streaming", "user-library-read"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, config.requestCode, request);

        // Set up views
        recycler = (RecyclerView) findViewById(R.id.recycler_track_list);
        recycler.setHasFixedSize(true);
        recyclerLayoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(recyclerLayoutManager);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Create media player
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        playback = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == config.requestCode) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                // Initialize spotify service
                SpotifyApi api = new SpotifyApi();
                api.setAccessToken(response.getAccessToken());
                SpotifyService spotify = api.getService();

                // Start loading track data
                progressBar.setVisibility(View.VISIBLE);
                new DownloadTrackDataTask(recycler, progressBar).execute(spotify);
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
        player.release();
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

    /**
     * Toggles playback of 30-second preview clip on selected track.
     * @param view View
     */
    public void togglePlaybackPreview(View view) {
        if(playback) {
            // Stop current track playback
            player.stop();
            player.reset();
            playbackButton.setImageResource(R.drawable.play_circle);

            if (playbackTrack.equals((String) view.getTag())) {
                playbackTrack = "";
                playbackButton = null;
                playback = false;
                return;
            }
        }

        // Load and play new track
        try {
            playbackTrack = (String) view.getTag();
            Log.d("Playback", "Loading track " + playbackTrack);
            player.setDataSource(playbackTrack);
            player.prepare();
            player.start();
            playbackButton = (ImageView) view;
            playbackButton.setImageResource(R.drawable.pause_circle);
            playback = true;
        } catch (Exception e) {
            Log.d("Playback", "Unable to play track " + e.getMessage());
        }
    }
}
