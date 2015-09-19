package com.kwohlford.smartplaylistmanager;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Kirsten on 19.09.2015.
 */
public class Config {

    private final static String CONFIG_FILENAME = "smartplaylistmanager.config";
    public final String CLIENT_ID;
    public final String REDIRECT_URI;
    public final int REQUEST_CODE;

    public Config(String clientId, String redirectURI, int requestCode) {
        CLIENT_ID = clientId;
        REDIRECT_URI = redirectURI;
        REQUEST_CODE = requestCode;
    }

    public static Config loadConfig(Context context) {
        String clientId = "";
        String redirectURI = "";
        int requestCode = 0;
        AssetManager assets = context.getAssets();
        InputStream stream = null;

        try {
            stream = assets.open(CONFIG_FILENAME);
            int data = stream.read();
            String contents = "";
            while(data != -1) {
                contents += (char) data;
                data = stream.read();
            }
            String[] parsed = contents.trim().split("\\n");
            Log.d("Loaded client id", parsed[0]);
            Log.d("Loaded redirect uri", parsed[1]);
            Log.d("Loaded request code", parsed[2]);
            clientId = parsed[0];
            redirectURI = parsed[1];
            requestCode = Integer.valueOf(parsed[2]);
        } catch (IOException ioe) {
            Log.e("Configuration failed", "Unable to read config file");
            ioe.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {}
            }
        }

        return new Config(clientId, redirectURI, requestCode);
    }


}
