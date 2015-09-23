package com.kwohlford.smartplaylistmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kwohlford.smartplaylistmanager.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Convenience methods for retrieving data from the database.
 *
 * query example:
 * Cursor results = database.query(                    // select from
 * DBHelper.DB_NAME,                             // table name
 * new String[] { DBHelper.COL_TAGS_NAME },            // columns
 * "category = " + type.id,                            // where clause
 * null,                                               // selection args
 * null,                                               // group by
 * null,                                               // having
 * null                                                // order by
 );
 *
 */
public class SourceTrackData extends DBDataSource {

    public static final String DB_NAME = "trackdata.db";
    public static final String TEST_DB_NAME = "trackdata_test.db";
    public static final int DATABASE_VERSION = 4;

    private boolean updateCachedTags;
    private ArrayList<Tag> cachedGenreTags;
    private ArrayList<Tag> cachedMoodTags;

    public SourceTrackData(Context context) {
        this(context, false);
    }

    public SourceTrackData(Context context, boolean accessTestDatabase) {
        super(context,
                accessTestDatabase ? TEST_DB_NAME : DB_NAME,
                DATABASE_VERSION,
                new TableTags(), new TableTrackTags(), new TableRatings());
        updateCachedTags = true;
    }

    @Override
    public void open() throws SQLException {
        super.open();
        updateCachedTags = true;
    }

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

    public void addTag(Tag tag) {
        // TODO
        updateCachedTags = true;
    }

    public void deleteTag(Tag tag) {
        // TODO
        updateCachedTags = true;
    }

    public void editTag(Tag prev, Tag edited) {
        // TODO
        updateCachedTags = true;
    }

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

//    SELECT tags.name, tags.category
//    FROM track_tags
//    JOIN tags ON tags.id = track_tags.tag_id
//    WHERE track_tags.track_id = "uri::song1"
//    AND tags.category = 1

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

    public void setTrackTags(String trackUri, Set<Map.Entry<Tag, Boolean>> changedEntries) {
        for(Map.Entry<Tag, Boolean> entry : changedEntries) {
            int tagKey = lookupTagKey(entry.getKey());
            if(entry.getValue()) {
                ContentValues values = new ContentValues();
                values.put(TableTrackTags.COL_TRACKID, trackUri);
                values.put(TableTrackTags.COL_TAGID, tagKey);
                Log.d("Database", "Adding tag " + entry.getKey().name + " to track " + trackUri);
                database.insert(
                        TableTrackTags.NAME,
                        null,
                        values
                );
            } else {
                Log.d("Database", "Deleting tag " + entry.getKey().name + " from track " + trackUri);
                database.delete(
                        TableTrackTags.NAME,
                        TableTrackTags.COL_TAGID + " = ? AND " + TableTrackTags.COL_TRACKID + " = ?",
                        new String[] { String.valueOf(tagKey), trackUri }
                );
            }
        }
    }

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
