package com.kwohlford.smartplaylistmanager;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

/**
 * RecyclerView adapter for saved track list.
 */
public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

    private TrackListing trackDataset;

    /**
     * Contains references to all views on a card.
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cv;
        private LinearLayout ll;
        private TextView songTitle;
        private TextView artist;
        private TextView albumName;
        private ImageView albumThumbnail;
        private ImageView playbackButton;
        private RatingBar songRating;
        private TextView tagsList;

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
            tagsList = (TextView) itemView.findViewById(R.id.txtTags);
        }
    }

    public TrackListAdapter(TrackListing trackList) {
        trackDataset = trackList;
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
        TrackData track = trackDataset.get(position);
        holder.songTitle.setText(track.songTitle);
        holder.artist.setText(track.artist);
        holder.albumName.setText(track.albumName);
        holder.playbackButton.setTag(track.previewUrl);
        holder.tagsList.setText(track.getTagsAsString());
        holder.songRating.setRating(track.getRating());
        holder.albumThumbnail.setImageResource(R.drawable.album_placeholder);
        new DownloadAlbumArtTask(holder.albumThumbnail).execute(track.albumArtUrl);
        holder.ll.getLayoutParams().height = TrackListActivity.LL_MIN_DP;
        holder.ll.setTag(R.string.tag_contracted);
        holder.cv.getLayoutParams().height = TrackListActivity.CARD_MIN_DP;
    }

    @Override
    public int getItemCount() {
        // Return the size of dataset (invoked by the layout manager)
        return trackDataset.size();
    }

}
