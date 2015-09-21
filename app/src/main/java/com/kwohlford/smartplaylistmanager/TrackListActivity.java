package com.kwohlford.smartplaylistmanager;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Screen for displaying list of user's saved tracks.
 */
public class TrackListActivity extends Activity implements
        ConnectionStateCallback {

    // Configuration loader
    private Config config;

    // Layout & view managers
    private RecyclerView recycler;

    // Layout size constants
    public static final int
            LL_MIN_DP = 120,
            LL_MAX_DP = 296,
            CARD_MIN_DP = 108,
            CARD_MAX_DP = 280;

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
        builder.setScopes(new String[]{"user-read-private", "user-library-read"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, config.authRequestCode, request);

        // Set up views
        recycler = (RecyclerView) findViewById(R.id.recycler_track_list);
        recycler.setHasFixedSize(true);
        RecyclerView.LayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
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

        if (requestCode == config.authRequestCode) {
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

            if (playbackTrack.equals(view.getTag())) {
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

    public void toggleExpandCard(final View view) {
        final LinearLayout ll = (LinearLayout) view;
        final CardView cv = (CardView) ll.findViewById(R.id.cv);

        Animation a;
        if(ll.getTag().equals(R.string.tag_contracted)) {
            // expand card
            a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    ll.getLayoutParams().height = (int) (LL_MIN_DP + ((LL_MAX_DP-LL_MIN_DP) * interpolatedTime));
                    cv.getLayoutParams().height = (int) (CARD_MIN_DP + ((CARD_MAX_DP-CARD_MIN_DP) * interpolatedTime));
                    ll.requestLayout();
                    cv.requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            ll.setTag(R.string.tag_expanded);
        } else {
            // contract card
            a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    ll.getLayoutParams().height = (int) (LL_MAX_DP + ((LL_MIN_DP-LL_MAX_DP) * interpolatedTime));
                    cv.getLayoutParams().height = (int) (CARD_MAX_DP + ((CARD_MIN_DP-CARD_MAX_DP) * interpolatedTime));
                    ll.requestLayout();
                    cv.requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            ll.setTag(R.string.tag_contracted);
        }

        a.setDuration(1000);
        view.startAnimation(a);

    }

}
