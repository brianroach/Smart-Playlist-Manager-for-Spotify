package com.kwohlford.smartplaylistmanager;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kwohlford.smartplaylistmanager.tracklist.DownloadTrackDataTask;
import com.kwohlford.smartplaylistmanager.tracklist.TrackListActivity;
import com.kwohlford.smartplaylistmanager.util.Config;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class StartActivity extends Activity {

    public static final String KEY_AUTH = "auth";
    private AuthenticationResponse authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Load API authentication data from config file
        Config config = Config.loadConfig(this);

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
                authToken = response;
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

    public void manageSavedTracks(View view) {
        Intent intent = new Intent(this, TrackListActivity.class);
        intent.putExtra(KEY_AUTH, authToken);
        startActivity(intent);
    }
}
