package com.kwohlford.smartplaylistmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kwohlford.smartplaylistmanager.db.SourceTrackData;
import com.kwohlford.smartplaylistmanager.playlist.CreatePlaylistActivity;
import com.kwohlford.smartplaylistmanager.tracklist.SyncTrackDataTask;
import com.kwohlford.smartplaylistmanager.tracklist.TrackListActivity;
import com.kwohlford.smartplaylistmanager.util.Config;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class StartActivity extends Activity implements
        ConnectionStateCallback {

    public static final String TAG = "AppMain";

    // API hook
    SpotifyService spotify;

    // Settings
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Load API authentication data from config file
        Config config = Config.loadConfig(this);

        // Load settings from sharedprefs
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Open database
        int dbVersion = prefs.getInt(Config.PREF_KEY_DBVERSION, Config.PREF_DEFAULT_DBVERSION);
        SourceTrackData dbInstance = SourceTrackData.getInstance();
        dbInstance.open(this, dbVersion);

        // Prompt user login
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(config.clientId,
                AuthenticationResponse.Type.TOKEN,
                config.redirectUri);
        builder.setScopes(new String[]{"user-read-private", "user-library-read"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, Config.REQCODE_AUTH, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == Config.REQCODE_AUTH) { // Result came from user login
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                // Initialize spotify service
                SpotifyApi api = new SpotifyApi();
                api.setAccessToken(response.getAccessToken());
                spotify = api.getService();

                // If app is being opened for the first time, start loading track data
                boolean isFirstOpen = prefs.getBoolean(Config.PREF_KEY_FIRSTOPEN, Config.PREF_DEFAULT_FIRSTOPEN);
                if(isFirstOpen) {
                    Log.d(TAG, "Running first-time initialization");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Config.PREF_KEY_FIRSTOPEN, false);
                    editor.apply();
                    runSpotifySync();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public void manageSavedTracks(View view) {
        Log.d(TAG, "Starting activity: track list");
        Intent intent = new Intent(this, TrackListActivity.class);
        startActivity(intent);
    }

    public void createNewPlaylist(View view) {
        Intent intent = new Intent(this, CreatePlaylistActivity.class);
        startActivity(intent);
    }

    public void showErrorDialog(String title, String message, String buttonText) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void runSpotifySync() {
        ProgressDialog progress = new ProgressDialog(this);
        new SyncTrackDataTask(progress).execute(spotify);
    }

    public void startSync(View view) {
        runSpotifySync();
    }
}
