package com.kwohlford.smartplaylistmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by Kirsten on 19.09.2015.
 */
public class DownloadAlbumArtTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadAlbumArtTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        Bitmap resizedResult = Bitmap.createScaledBitmap(result, bmImage.getHeight(), bmImage.getHeight(), false);
        bmImage.setImageBitmap(resizedResult);
    }
}