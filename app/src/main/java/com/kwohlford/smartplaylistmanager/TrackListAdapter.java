package com.kwohlford.smartplaylistmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;
import java.util.List;

import kaaes.spotify.webapi.android.models.SavedTrack;

/**
 * Created by Kirsten on 18.09.2015.
 */
public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {
    private List<TrackData> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView songTitle;
        TextView artist;
        TextView albumName;
        ImageView albumThumbnail;
        public ViewHolder(View v) {
            super(v);
            cv = (CardView)itemView.findViewById(R.id.cv);
            songTitle = (TextView)itemView.findViewById(R.id.song_title);
            artist = (TextView)itemView.findViewById(R.id.artist);
            albumName = (TextView)itemView.findViewById(R.id.album_name);
            albumThumbnail = (ImageView)itemView.findViewById(R.id.album_thumbnail);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TrackListAdapter(List<TrackData> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TrackListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_list_item, parent, false);



        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.songTitle.setText(mDataset.get(position).songTitle);
        holder.artist.setText(mDataset.get(position).artist);
        holder.albumName.setText(mDataset.get(position).albumName);

        new DownloadAlbumArtTask(holder.albumThumbnail).execute(mDataset.get(position).albumArtUrl);

//        holder.albumThumbnail.setImageResource(mDataset.get(position).albumThumbnail);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}
