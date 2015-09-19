package com.kwohlford.smartplaylistmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Downloads and sets album art to an ImageView.
 */
public class DownloadAlbumArtTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "DownloadAlbumArt";

    private ImageView albumImage;

    public DownloadAlbumArtTask(ImageView albumImage) {
        this.albumImage = albumImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap albumBmp = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            albumBmp = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(TAG, "Unable to download album art");
        }
        return albumBmp;
    }

    protected void onPostExecute(Bitmap result) {
        Bitmap resizedResult = Bitmap.createScaledBitmap(result, albumImage.getHeight(), albumImage.getHeight(), false);
        albumImage.setImageBitmap(resizedResult);
    }
}