package com.kwohlford.smartplaylistmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kwohlford.smartplaylistmanager.tracklist.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Convenience methods for retrieving data from the trackdata database.
 */
public class SourceTrackData extends DBDataSource {

    // Basic database info
    public static final String DB_NAME = "trackdata.db";
    public static final int DATABASE_VERSION = 4;

    // Cached lists of retrieved tags to reduce database calls
    private ArrayList<Tag> cachedGenreTags;
    private ArrayList<Tag> cachedMoodTags;

    // True if tag caches need to be updated, false otherwise
    private boolean updateCachedTags;

    public SourceTrackData(Context context) {
        super(context,
                DB_NAME,
                DATABASE_VERSION,
                new TableTags(), new TableTrackTags(), new TableRatings());
        updateCachedTags = true;
    }

    @Override
    public void open() throws SQLException {
        super.open();
        updateCachedTags = true;
    }

    /**
     * @param type Category of tags to retrieve
     * @return List of all available tags in the category
     */
    public ArrayList<Tag> getTagsByType(Tag.TagType type) {
        if(updateCachedTags) {
            cachedGenreTags = updateCachedTags(Tag.TagType.GENRE);
            cachedMoodTags = updateCachedTags(Tag.TagType.MOOD);
            updateCachedTags = false;
        }

        switch(type) {
            case GENRE:
                return cachedGenreTags;
            case MOOD:
                return cachedMoodTags;
            default:
                return new ArrayList<>();
        }
    }

    /**
     * Queries the database to update the cached tag lists.
     * @param type Category of tags to query for
     * @return Updated list of tags
     */
    private ArrayList<Tag> updateCachedTags(Tag.TagType type) {
        Cursor results = database.query(
                TableTags.NAME,
                new String[] { TableTags.COL_NAME },
                TableTags.COL_CATEGORY + " = " + type.id,
                null, null, null, null
        );
        results.moveToFirst();

        ArrayList<Tag> tags = new ArrayList<>();
        while(!results.isAfterLast()) {
            tags.add(new Tag(results.getString(0), type));
            results.moveToNext();
        }
        results.close();
        return tags;
    }

    /**
     * TODO
     * @param tag
     */
    public void addTag(Tag tag) {
        updateCachedTags = true;
    }

    /**
     * TODO
     * @param tag
     */
    public void deleteTag(Tag tag) {
        updateCachedTags = true;
    }

    /**
     * TODO
     * @param prev
     * @param edited
     */
    public void editTag(Tag prev, Tag edited) {
        updateCachedTags = true;
    }

    /**
     * Queries the database to get a track's rating.
     * @param trackUri Uri of track to search for
     * @return User-defined rating of the track (or 0 if none has been set)
     */
    public float getRating(String trackUri) {
        Cursor results = database.query(
                TableRatings.NAME,
                new String[]{ TableRatings.COL_RATING },
                TableRatings.COL_ID + " = ?",
                new String[]{ trackUri },
                null, null, null
        );
        results.moveToFirst();
        float rating = results.isAfterLast() ? 0 : results.getFloat(0);
        results.close();
        return rating;
    }

    /**
     * Inserts or overwrites a track's rating in the database.
     * @param trackUri Track to set rating for
     * @param rating User rating out of 5
     */
    public void setRating(String trackUri, float rating) {
        ContentValues values = new ContentValues();
        values.put(TableRatings.COL_ID, trackUri);
        values.put(TableRatings.COL_RATING, rating);
        Log.d("Database", "Setting rating " + rating + " on track " + trackUri);
        database.insertWithOnConflict(
                TableRatings.NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * Queries the database to get all tags associated with a track.
     * @param trackUri Track to search for
     * @param type Category of tags to retrieve
     * @return List of tags associated with the track
     */
    public HashMap<Tag, Boolean> getTrackTags(String trackUri, Tag.TagType type) {
        String query =
                "SELECT tags.name " +
                        "FROM track_tags " +
                        "JOIN tags ON tags._id = track_tags.tag_id " +
                        "WHERE track_tags.track_id = ? AND tags.category = ?";
        Cursor results = database.rawQuery(
                query,
                new String[] {trackUri, String.valueOf(type.id)}
        );
        results.moveToFirst();

        ArrayList<Tag> allTags = getTagsByType(type);
        HashMap<Tag, Boolean> tagMapping = new HashMap<>();
        for(Tag t : allTags) {
            tagMapping.put(t, false);
        }

        while(!results.isAfterLast()) {
            tagMapping.put(new Tag(results.getString(0), type), true);
            results.moveToNext();
        }
        results.close();
        return tagMapping;
    }

    /**
     * Updates the tags associated with a track based on a list of changes.
     * @param trackUri Track uri
     * @param changedEntries Tags which have changed since the last update, mapped to a boolean
     *                       indicating whether to add (if true) or remove (if false) the tag
     */
    public void setTrackTags(String trackUri, Set<Map.Entry<Tag, Boolean>> changedEntries) {
        for(Map.Entry<Tag, Boolean> entry : changedEntries) {
            int tagKey = lookupTagKey(entry.getKey());
            if(entry.getValue()) {
                Log.d("Database", "Adding tag " + entry.getKey().name + " to track " + trackUri);
                addTrackTag(trackUri, tagKey);
            } else {
                Log.d("Database", "Deleting tag " + entry.getKey().name + " from track " + trackUri);
                deleteTrackTag(trackUri, tagKey);
            }
        }
    }

    /**
     * Adds a new entry in the database linking a tag to a track.
     * @param trackUri Track uri
     * @param tagKey PK of tag entry in 'tags' table
     */
    private void addTrackTag(String trackUri, int tagKey) {
        ContentValues values = new ContentValues();
        values.put(TableTrackTags.COL_TRACKID, trackUri);
        values.put(TableTrackTags.COL_TAGID, tagKey);
        database.insert(
                TableTrackTags.NAME,
                null,
                values
        );
    }

    /**
     * Deletes an entry in the database linking a tag to a track.
     * @param trackUri Track uri
     * @param tagKey PK of tag entry in 'tags' table
     */
    private void deleteTrackTag(String trackUri, int tagKey) {
        database.delete(
                TableTrackTags.NAME,
                TableTrackTags.COL_TAGID + " = ? AND " + TableTrackTags.COL_TRACKID + " = ?",
                new String[]{String.valueOf(tagKey), trackUri}
        );
    }

    /**
     * Looks up the primary key associated with a stored tag.
     * @param tag Tag to search for
     * @return Integer primary key of that tag's database entry.
     */
    private int lookupTagKey(Tag tag) {
        Cursor results = database.query(
                TableTags.NAME,
                new String[] { TableTags.COL_ID },
                TableTags.COL_CATEGORY + " = ? AND " + TableTags.COL_NAME + " = ?",
                new String[] { String.valueOf(tag.type.id), tag.name },
                null, null, null
        );
        results.moveToFirst();
        int id = results.isAfterLast() ? -1 : results.getInt(0);
        results.close();
        return id;
    }

}
