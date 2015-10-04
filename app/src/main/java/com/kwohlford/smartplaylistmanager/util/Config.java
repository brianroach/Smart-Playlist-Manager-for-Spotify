package com.kwohlford.smartplaylistmanager.util;

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

    public final static int REQCODE_AUTH = 3333;

    public final static String PREF_KEY_FIRSTOPEN = "firsttimeopen";
    public final static boolean PREF_DEFAULT_FIRSTOPEN = true;

    public final static String PREF_KEY_DBVERSION = "dbversion";
    public final static int PREF_DEFAULT_DBVERSION = 1;

    public final String clientId;
    public final String redirectUri;

    private Config(String clientId, String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    /**
     * Attempt to load config file and retrieve authentication data.
     * @param context Context for accessing asset folder
     * @return Initialized Config object
     */
    public static Config loadConfig(Context context) {
        String clientId = "";
        String redirectURI = "";
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
            redirectURI = parsed[1];
        } catch (IOException ioe) {
            Log.e(TAG, "Unable to read config file");
            ioe.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.d(TAG, "Unable to close config file.");
                }
            }
        }

        return new Config(clientId, redirectURI);
    }


}
