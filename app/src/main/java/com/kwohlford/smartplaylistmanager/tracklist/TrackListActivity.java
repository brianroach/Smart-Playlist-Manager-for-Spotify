package com.kwohlford.smartplaylistmanager.tracklist;

import android.animation.AnimatorSet;
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
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kwohlford.smartplaylistmanager.R;
import com.kwohlford.smartplaylistmanager.StartActivity;
import com.kwohlford.smartplaylistmanager.db.SourceTrackData;
import com.kwohlford.smartplaylistmanager.util.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Screen for displaying list of user's saved tracks.
 */
public class TrackListActivity extends Activity {

    // Layout & view managers
    private RecyclerView recycler;
    private TrackListAdapter recyclerAdapter;

    // Layout size constants
    public static final int
            LL_MIN_DP = 80,
            LL_MAX_DP = 230,
            CARD_MIN_DP = 70,
            CARD_MAX_DP = 220;

    // Vars for tracking playback of preview clips
    private boolean playback;
    private ImageView playbackButton;
    private MediaPlayer player;
    private String playbackTrack;

    // Database source
    public SourceTrackData database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracklist);
        Log.d("TrackList", "Creating activity: track list");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get database instance
        database = SourceTrackData.getInstance();

        // Set up views
        recycler = (RecyclerView) findViewById(R.id.recycler_track_list);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        setRecyclerAdapter(new TrackListAdapter(database.tracks, this));

        // Create media player
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        playback = false;
    }

    @Override
    protected void onPause() {
        player.release();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            database.tracks.getTrackForUri(playbackTrack).previewPlaying = false;

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
            player.setDataSource(database.tracks.getTrackForUri(playbackTrack).previewUrl);
            player.prepare();
            player.start();
            playbackButton = (ImageView) view;
            playbackButton.setImageResource(R.drawable.pause_circle);
            database.tracks.getTrackForUri(playbackTrack).previewPlaying = true;
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
        final ImageView arrow = (ImageView) ll.findViewById(R.id.imgArrow);

        Animation cardAnim;
        RotateAnimation rotateAnim;
        if(ll.getTag().equals(R.string.tag_collapsed)) {
            // expand card
            cardAnim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    // convert dpi to px
                    float scale = getBaseContext().getResources().getDisplayMetrics().density;
                    float llMinPx = LL_MIN_DP * scale + 0.5f;
                    float llMaxPx = LL_MAX_DP * scale + 0.5f;
                    float cvMinPx = CARD_MIN_DP * scale + 0.5f;
                    float cvMaxPx = CARD_MAX_DP * scale + 0.5f;

                    ll.getLayoutParams().height = (int) (llMinPx + ((llMaxPx-llMinPx) * interpolatedTime));
                    cv.getLayoutParams().height = (int) (cvMinPx + ((cvMaxPx-cvMinPx) * interpolatedTime));
                    ll.requestLayout();
                    cv.requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            rotateAnim = new RotateAnimation(0.0f, 90,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            rotateAnim.setFillAfter(true);
            ll.setTag(R.string.tag_expanded);
        } else {
            // collapse card
            cardAnim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    // convert dpi to px
                    float scale = getBaseContext().getResources().getDisplayMetrics().density;
                    float llMinPx = LL_MIN_DP * scale + 0.5f;
                    float llMaxPx = LL_MAX_DP * scale + 0.5f;
                    float cvMinPx = CARD_MIN_DP * scale + 0.5f;
                    float cvMaxPx = CARD_MAX_DP * scale + 0.5f;

                    ll.getLayoutParams().height = (int) (llMaxPx + ((llMinPx-llMaxPx) * interpolatedTime));
                    cv.getLayoutParams().height = (int) (cvMaxPx + ((cvMinPx-cvMaxPx) * interpolatedTime));
                    ll.requestLayout();
                    cv.requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };
            rotateAnim = new RotateAnimation(0.0f, 0,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            rotateAnim.setFillAfter(true);
            ll.setTag(R.string.tag_collapsed);
        }

        rotateAnim.setDuration(500);
        cardAnim.setDuration(500);
        arrow.startAnimation(rotateAnim);
        view.startAnimation(cardAnim);
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
                database.tracks.getTrackForUri((String) card.getTag()),
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
                database.tracks.getTrackForUri((String) card.getTag()),
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

        ArrayList<Tag> allTags = database.tracks.getTags(type);
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
                        setTrackTags(track, type, newTags);
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

    /**
     * Start activity for editing available tags.
     * @param type Type of tags to edit
     */
    private void openEditTagsScreen(Tag.TagType type) {
        Intent intent = new Intent(this, EditTagsActivity.class);
        intent.putExtra(EditTagsActivity.KEY_TYPE, type.id);
        intent.putParcelableArrayListExtra(EditTagsActivity.KEY_TAGLIST, database.tracks.getTags(type));
        startActivity(intent);
    }

    /**
     * Sets the tags associated with a track and calls the database to store the changes.
     * @param track Track to edit
     * @param type Type of tags
     * @param newTags List of tags to associate with the track
     */
    private void setTrackTags(TrackData track, Tag.TagType type, ArrayList<Tag> newTags) {
        ArrayList<Tag> oldTags;
        switch(type) {
            case GENRE:
                oldTags = track.genreTags;
                track.genreTags = newTags;
                break;
            case MOOD:
                oldTags = track.moodTags;
                track.moodTags = newTags;
                break;
            default:
                oldTags = new ArrayList<>();
        }
        Set<Tag> deleted = new HashSet<>(oldTags);
        Set<Tag> added = new HashSet<>(newTags);
        deleted.removeAll(added);
        added.removeAll(deleted);

        database.setTrackTags(track.uri, new ArrayList<>(added), new ArrayList<>(deleted));
    }

}
