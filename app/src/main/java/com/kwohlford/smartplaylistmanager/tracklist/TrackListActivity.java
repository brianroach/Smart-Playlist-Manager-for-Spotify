package com.kwohlford.smartplaylistmanager.tracklist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;

import com.kwohlford.smartplaylistmanager.R;
import com.kwohlford.smartplaylistmanager.StartActivity;
import com.kwohlford.smartplaylistmanager.db.SourceTrackData;
import com.kwohlford.smartplaylistmanager.util.Config;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;

import java.util.ArrayList;
import java.util.HashMap;

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
    private TrackListAdapter recyclerAdapter;

    // Layout size constants
    public static final int
            LL_MIN_DP = 120,
            LL_MAX_DP = 296,
            CARD_MIN_DP = 108,
            CARD_MAX_DP = 280;

    // Loading bar
    public ProgressBar progressBar;

    // Vars for tracking playback of preview clips
    private boolean playback;
    private ImageView playbackButton;
    private MediaPlayer player;
    private String playbackTrack;

    // Track list
    public TrackListing tracks;

    // Database source
    public SourceTrackData database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracklist);

        // Make sure user successfully logged in
        Bundle extras = getIntent().getExtras();
        AuthenticationResponse authResponse = (AuthenticationResponse) extras.get(StartActivity.KEY_AUTH);
        if(authResponse != null) {
            // Set up views
            recycler = (RecyclerView) findViewById(R.id.recycler_track_list);
            recycler.setHasFixedSize(true);
            recycler.setLayoutManager(new LinearLayoutManager(this));

            // Create media player
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            playback = false;

            // Open database
            database = new SourceTrackData(this);
            database.open();

            // Initialize spotify service
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(authResponse.getAccessToken());
            SpotifyService spotify = api.getService();

            // Start loading track data
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
            new DownloadTrackDataTask(this).execute(spotify);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == Config.REQCODE_EDITTAGS) { // Result came from edit tags activity
            Bundle extras = intent.getExtras();
            ArrayList<Tag> changedTags = extras.getParcelableArrayList(EditTagsActivity.KEY_TAGLIST);
            if (changedTags != null) {
                for(Tag t : changedTags) {
                    if(t.changeFlag == Tag.FLAG_ADDED) {
                        Log.d("Edit Tags", "Adding tag " + t.name);
                        database.addTag(t);
                        tracks.addTag(t);
                    } else if(t.changeFlag == Tag.FLAG_DELETED) {
                        Log.d("Edit Tags", "Deleting tag " + t.name);
                        ArrayList<String> tracksWithTag = database.getTrackUrisWithTag(t);
                        for(String s : tracksWithTag) {
                            tracks.getTrackForUri(s).getTags(t.type).remove(t);
                        }
                        tracks.deleteTag(t);
                        database.deleteTag(t);
                    } else if(t.changeFlag == Tag.FLAG_CHANGED) {
                        Log.d("Edit Tags", "Renaming tag " + t.prevName + " to " + t.name);
                        Tag oldTag = new Tag(t.prevName, t.type);
                        ArrayList<String> tracksWithTag = database.getTrackUrisWithTag(oldTag);
                        for(String s : tracksWithTag) {
                            ArrayList<Tag> trackTags = tracks.getTrackForUri(s).getTags(t.type);
                            trackTags.remove(oldTag);
                            trackTags.add(t);
                        }
                        tracks.addTag(t);
                        tracks.deleteTag(oldTag);
                        database.editTag(oldTag, t);
                    }
                    t.changeFlag = Tag.FLAG_NONE;
                }
                setRecyclerAdapter(new TrackListAdapter(tracks));
            }
        }
    }

    @Override
    protected void onResume() {
        database.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        database.close();
        super.onPause();
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
        CardView card = (CardView) view.getParent().getParent();
        if(playback) {
            // Stop current track playback
            player.stop();
            player.reset();
            playbackButton.setImageResource(R.drawable.play_circle);
            tracks.getTrackForUri(playbackTrack).previewPlaying = false;

            if (playbackTrack.equals(card.getTag())) {
                playbackTrack = "";
                playbackButton = null;
                playback = false;
                return;
            }
        }

        // Load and play new track
        try {
            Log.d("Playback", "Loading track " + playbackTrack);
            playbackTrack = (String) card.getTag();
            player.setDataSource(tracks.getTrackForUri(playbackTrack).previewUrl);
            player.prepare();
            player.start();
            playbackButton = (ImageView) view;
            playbackButton.setImageResource(R.drawable.pause_circle);
            tracks.getTrackForUri(playbackTrack).previewPlaying = true;
            playback = true;
        } catch (Exception e) {
            Log.d("Playback", "Unable to play track " + e.getMessage());
        }
    }

    /**
     * Expands or collapses card to display/hide additional info.
     * @param view Layout wrapper containing card
     */
    public void toggleExpandCard(final View view) {
        final LinearLayout ll = (LinearLayout) view;
        final CardView cv = (CardView) ll.findViewById(R.id.cv);

        Animation a;
        if(ll.getTag().equals(R.string.tag_collapsed)) {
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
            // collapse card
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
            ll.setTag(R.string.tag_collapsed);
        }

        a.setDuration(500);
        view.startAnimation(a);
    }

    /**
     * @param adapter RecyclerView adapter for track listing
     */
    public void setRecyclerAdapter(TrackListAdapter adapter) {
        recycler.setAdapter(adapter);
        recyclerAdapter = adapter;
    }

    /**
     * Opens a new genre tag selector dialog.
     * @param view Button pressed
     */
    public void openGenreDialog(View view) {
        CardView card = (CardView) view.getParent().getParent().getParent();
        createTagDialog(
                "Select Genre(s)",
                tracks.getTrackForUri((String) card.getTag()),
                Tag.TagType.GENRE,
                (TextView) card.findViewById(R.id.genreTags)
        ).show();
    }

    /**
     * Opens a new mood tag selector dialog.
     * @param view Button pressed
     */
    public void openMoodDialog(View view) {
        CardView card = (CardView) view.getParent().getParent().getParent();
        createTagDialog(
                "Select Mood(s)",
                tracks.getTrackForUri((String) card.getTag()),
                Tag.TagType.MOOD,
                (TextView) card.findViewById(R.id.moodTags)
        ).show();
    }

    /**
     * Creates a checkbox dialog for selecting tags.
     * @param title Title of dialog
     * @param track Track being edited
     * @param type Tag type
     * @param txtTags Text view to update with new tags
     * @return New tag selector dialog
     */
    private AlertDialog createTagDialog(
            String title,
            final TrackData track,
            final Tag.TagType type,
            final TextView txtTags) {

        ArrayList<Tag> allTags = tracks.getTagsByType(type);
        ArrayList<Tag> trackTags = track.getTags(type);

        final CharSequence[] values = new CharSequence[allTags.size()];
        final boolean[] state = new boolean[allTags.size()];
        for(int i = 0; i < allTags.size(); i++) {
            values[i] = allTags.get(i).name;
            state[i] = trackTags.contains(allTags.get(i));
            Log.d("Loading tag", values[i] + ": " + state[i]);
        }

        return new AlertDialog.Builder(this)
                .setTitle(title)
                .setMultiChoiceItems(values, state, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index, boolean isChecked) {
                        state[index] = isChecked;
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        ArrayList<Tag> newTags = new ArrayList<>();
                        for (int i = 0; i < values.length; i++) {
                            Log.d("Setting tag", values[i] + ": " + state[i]);
                            if(state[i]) newTags.add(new Tag((String) values[i], type));
                        }
                        track.setTags(type, newTags);
                        dialog.dismiss();
                        txtTags.setText(track.getTagsAsString(type));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Edit tags", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openEditTagsScreen(type);
                    }
                })
                .create();
    }

    private void openEditTagsScreen(Tag.TagType type) {
        Intent intent = new Intent(this, EditTagsActivity.class);
        intent.putExtra(EditTagsActivity.KEY_TYPE, type.id);
        intent.putParcelableArrayListExtra(EditTagsActivity.KEY_TAGLIST,
                type == Tag.TagType.GENRE ? tracks.genreTags : tracks.moodTags);
        startActivityForResult(intent, Config.REQCODE_EDITTAGS);
    }

}
