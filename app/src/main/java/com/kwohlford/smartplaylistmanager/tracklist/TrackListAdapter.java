package com.kwohlford.smartplaylistmanager.tracklist;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.kwohlford.smartplaylistmanager.R;
import com.kwohlford.smartplaylistmanager.db.SourceTrackData;

/**
 * RecyclerView adapter for saved track list.
 */
public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

    private final TrackListing trackDataset;
    private final Context context;

    /**
     * Contains references to all views on a card.
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cv;
        private final LinearLayout ll;
        private final TextView songTitle;
        private final TextView artist;
        private final TextView albumName;
        private final ImageView albumThumbnail;
        private final ImageView playbackButton;
        private final RatingBar songRating;
        private final TextView genreTags;
        private final TextView moodTags;

        protected ViewHolder(View v) {
            super(v);
            cv = (CardView) itemView.findViewById(R.id.cv);
            ll = (LinearLayout) itemView.findViewById(R.id.ll);
            songTitle = (TextView) itemView.findViewById(R.id.song_title);
            artist = (TextView) itemView.findViewById(R.id.artist);
            albumName = (TextView) itemView.findViewById(R.id.album_name);
            albumThumbnail = (ImageView) itemView.findViewById(R.id.album_thumbnail);
            playbackButton = (ImageView) itemView.findViewById(R.id.playback_button);
            songRating = (RatingBar) itemView.findViewById(R.id.songRating);
            genreTags = (TextView) itemView.findViewById(R.id.genreTags);
            moodTags = (TextView) itemView.findViewById(R.id.moodTags);
        }
    }

    public TrackListAdapter(TrackListing trackList, Context context) {
        trackDataset = trackList;
        this.context = context;
    }

    @Override
    public TrackListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view (invoked by the layout manager)
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item_expanded, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Replace the contents of a view (invoked by the layout manager)

        final TrackData track = trackDataset.get(position);

        // set up layout containers
        float scale = context.getResources().getDisplayMetrics().density;
        holder.ll.getLayoutParams().height = (int) (TrackListActivity.LL_MIN_DP * scale + 0.5f);
        holder.cv.getLayoutParams().height = (int) (TrackListActivity.CARD_MIN_DP * scale + 0.5f);
        holder.ll.setTag(R.string.tag_collapsed);
        holder.cv.setTag(track.uri);

        // set text views
        holder.songTitle.setText(track.songTitle);
        holder.artist.setText(track.artist);
        holder.albumName.setText(track.albumName);
        holder.genreTags.setText(track.getTagsAsString(Tag.TagType.GENRE));
        holder.moodTags.setText(track.getTagsAsString(Tag.TagType.MOOD));

        // set rating
        holder.songRating.setRating(track.rating);
        holder.songRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    track.rating = rating;
                    SourceTrackData.getInstance().setRating(track.uri, rating);
                }
            }
        });

        // set images
        holder.playbackButton.setImageResource(track.previewPlaying ? R.drawable.pause_circle : R.drawable.play_circle);
        holder.albumThumbnail.setImageResource(R.drawable.album_placeholder);
        new DownloadAlbumArtTask(holder.albumThumbnail).execute(track.albumArtUrl);
    }

    @Override
    public int getItemCount() {
        // Return the size of dataset (invoked by the layout manager)
        return trackDataset.size();
    }

}
