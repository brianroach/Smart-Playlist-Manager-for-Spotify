package com.kwohlford.smartplaylistmanager;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads and stores configuration data.
 */
public class Config {

    private final static String TAG = "Config";
    private final static String CONFIG_FILENAME = "smartplaylistmanager.config";
    public final String clientId;
    public final String redirectUri;
    public final int authRequestCode;

    private Config(String clientId, String redirectUri, int authRequestCode) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.authRequestCode = authRequestCode;
    }

    /**
     * Attempt to load config file and retrieve authentication data.
     * @param context Context for accessing asset folder
     * @return Initialized Config object
     */
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
            clientId = parsed[0];
            Log.d(TAG, "Loaded client id: " + parsed[0]);
            redirectURI = parsed[1];
            Log.d(TAG, "Loaded redirect uri: " + parsed[1]);
            requestCode = Integer.valueOf(parsed[2]);
            Log.d(TAG, "Loaded request code: " + parsed[2]);
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
