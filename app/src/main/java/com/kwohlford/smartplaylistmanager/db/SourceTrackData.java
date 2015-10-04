package com.kwohlford.smartplaylistmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kwohlford.smartplaylistmanager.tracklist.Tag;
import com.kwohlford.smartplaylistmanager.tracklist.TrackData;
import com.kwohlford.smartplaylistmanager.tracklist.TrackListing;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Convenience methods for retrieving data from the trackdata database. Must be opened before
 * any methods will return data.
 */
public class SourceTrackData extends DBDataSource {

    private static final String TAG = "Database";

    // Singleton instance of datasource
    private static SourceTrackData instance;

    // Basic database info
    public static final String DB_NAME = "trackdata.db";

    // Whether or not the database has been opened
    private boolean open;

    // Cached track list (reduces db calls)
    public TrackListing tracks;

    /**
     * Private constructor to prevent outside instantiation.
     */
    private SourceTrackData() {
        open = false;
    }

    /**
     * @return Singleton instance of the data source
     */
    public static SourceTrackData getInstance() {
        if(instance == null) {
            instance = new SourceTrackData();
        }
        return instance;
    }

    /**
     * Open the database. Must be called before any other methods will return data.
     * @param context Context
     * @throws SQLException If the database cannot be accessed
     */
    public void open(Context context, int version) throws SQLException {
        super.open(context,
                DB_NAME,
                version,
                new TableTags(), new TableTrackTags(), new TableTracks());
        open = true;
        tracks = loadAllTracks();
    }

    /**
     * Loads complete list of tracks from the db.
     * @return List of all tracks and their data
     */
    private TrackListing loadAllTracks() {
        Cursor results = database.query(
                TableTracks.NAME,
                null, null, null, null, null, null
        );
        results.moveToFirst();

        HashMap<String,TrackData> tracks = new HashMap<>();
        while(!results.isAfterLast()) {
            String uri = results.getString(0);
            TrackData track = new TrackData(
                    uri,
                    results.getString(1),
                    results.getString(2),
                    results.getString(3),
                    results.getString(4),
                    results.getString(5)
            );
            track.rating = results.getFloat(6);
            track.setTags(Tag.TagType.GENRE, getTrackTags(uri, Tag.TagType.GENRE));
            track.setTags(Tag.TagType.MOOD, getTrackTags(uri, Tag.TagType.MOOD));
            tracks.put(uri, track);
            results.moveToNext();
        }
        results.close();

        TrackListing trackList = new TrackListing(tracks);
        trackList.setTags(Tag.TagType.GENRE, getTagsByType(Tag.TagType.GENRE));
        trackList.setTags(Tag.TagType.MOOD, getTagsByType(Tag.TagType.MOOD));

        return trackList;
    }

    /**
     * Adds a new track to the database.
     * @param track Track to add
     */
    public void addTrack(TrackData track) {
        if(!open) return;

        Log.d(TAG, "Adding track " + track.songTitle);
        ContentValues values = new ContentValues();
        values.put(TableTracks.COL_ID, track.uri);
        values.put(TableTracks.COL_TITLE, track.songTitle);
        values.put(TableTracks.COL_ARTIST, track.artist);
        values.put(TableTracks.COL_ALBUM, track.albumName);
        values.put(TableTracks.COL_PREVIEW, track.previewUrl);
        values.put(TableTracks.COL_ALBUMART, track.albumArtUrl);
        values.put(TableTracks.COL_RATING, track.rating);
        database.insert(
                TableTracks.NAME,
                null,
                values
        );

        tracks.addTrack(track);
    }

    /**
     * Removes a track from the database.
     * @param track Track to delete
     */
    public void deleteTrack(TrackData track) {
        if(!open) return;

        // Delete all associations with the track
        database.delete(
                TableTrackTags.NAME,
                TableTrackTags.COL_TRACKID + " = ?",
                new String[]{track.uri}
        );

        // Delete the track itself
        Log.d(TAG, "Deleting track " + track.songTitle);
        database.delete(
                TableTracks.NAME,
                TableTracks.COL_ID + " = ?",
                new String[]{track.uri}
        );
        tracks.removeTrack(track);
    }

    /**
     * Queries the database to retrieve a list of all available tags in a given category.
     * @param type Category of tags to retrieve
     * @return List of all available tags in the category
     */
    private ArrayList<Tag> getTagsByType(Tag.TagType type) {
        if(!open) return new ArrayList<>();

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
     * Inserts a new tag entry into the database.
     * @param tag Tag to add
     */
    public void addTag(Tag tag) {
        if(!open) return;

        Log.d(TAG, "Adding tag " + tag.name);
        ContentValues values = new ContentValues();
        values.put(TableTags.COL_NAME, tag.name);
        values.put(TableTags.COL_CATEGORY, tag.type.id);
        database.insert(
                TableTags.NAME,
                null,
                values
        );
        tracks.addTag(tag);
    }

    /**
     * Deletes a tag and all associations with it from the database.
     * @param tag Tag to delete
     */
    public void deleteTag(Tag tag) {
        if(!open) return;

        // Remove from cached track list
        ArrayList<String> tracksWithTag = getTrackUrisWithTag(tag);
        for(String s : tracksWithTag) {
            tracks.getTrackForUri(s).getTags(tag.type).remove(tag);
        }
        tracks.deleteTag(tag);

        // Delete all associations with the tag
        int tagId = lookupTagKey(tag);
        database.delete(
                TableTrackTags.NAME,
                TableTrackTags.COL_TAGID + " = ?",
                new String[]{String.valueOf(tagId)}
        );

        // Delete the tag itself
        Log.d(TAG, "Deleting tag " + tag.name);
        database.delete(
                TableTags.NAME,
                TableTags.COL_ID + " = ?",
                new String[]{String.valueOf(tagId)}
        );
    }

    /**
     * Renames a tag in the database.
     * @param prev Tag to be updated
     * @param newName New name for the tag
     */
    public void editTag(Tag prev, String newName) {
        if(!open) return;

        // Update name in cached track list
        ArrayList<String> tracksWithTag = getTrackUrisWithTag(prev);
        for(String s : tracksWithTag) {
            ArrayList<Tag> trackTags = tracks.getTrackForUri(s).getTags(prev.type);
            trackTags.get(trackTags.indexOf(prev)).name = newName;
        }
        ArrayList<Tag> allTags = tracks.getTags(prev.type);
        allTags.get(allTags.indexOf(prev)).name = newName;

        // Update in database
        Log.d("Edit Tags", "Renaming tag " + prev.name + " to " + newName);
        ContentValues values = new ContentValues();
        values.put(TableTags.COL_NAME, newName);
        database.update(
                TableTags.NAME,
                values,
                TableTags.COL_NAME + " = ? AND " + TableTags.COL_CATEGORY + " = ?",
                new String[]{prev.name, String.valueOf(prev.type.id)}
        );
    }

    /**
     * Queries the database to retrieve a list of tracks which are associated with a tag.
     * @param tag Tag to search for
     * @return List of tracks associated with the tag
     */
    public ArrayList<String> getTrackUrisWithTag(Tag tag) {
        if(!open) return new ArrayList<>();

        String query =
                "SELECT track_tags.track_id " +
                        "FROM track_tags " +
                        "JOIN tags ON tags._id = track_tags.tag_id " +
                        "WHERE tags.category = ? AND tags.name = ?";
        Cursor results = database.rawQuery(
                query,
                new String[]{String.valueOf(tag.type.id), tag.name}
        );
        results.moveToFirst();

        ArrayList<String> tracks = new ArrayList<>();
        while(!results.isAfterLast()) {
            tracks.add(results.getString(0));
            results.moveToNext();
        }
        results.close();
        return tracks;
    }

    /**
     * Updates a track's rating in the database.
     * @param trackUri Track to set rating for
     * @param rating User rating out of 5
     */
    public void setRating(String trackUri, float rating) {
        if(!open) return;

        ContentValues values = new ContentValues();
        values.put(TableTracks.COL_RATING, rating);
        Log.d(TAG, "Setting rating " + rating + " on track " + trackUri);

        database.update(
                TableTracks.NAME,
                values,
                TableTracks.COL_ID + " = ?",
                new String[]{trackUri}
        );
    }

    /**
     * Queries the database to get all tags associated with a track.
     * @param trackUri Track to search for
     * @param type Category of tags to retrieve
     * @return List of tags associated with the track
     */
    public ArrayList<Tag> getTrackTags(String trackUri, Tag.TagType type) {
        if(!open) return new ArrayList<>();

        String query =
                "SELECT tags.name " +
                        "FROM track_tags " +
                        "JOIN tags ON tags._id = track_tags.tag_id " +
                        "WHERE track_tags.track_id = ? AND tags.category = ?";
        Cursor results = database.rawQuery(
                query,
                new String[]{trackUri, String.valueOf(type.id)}
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
     * Updates the tags associated with a track based on a list of changes.
     * @param trackUri Track uri
     * @param addedEntries Tags which have been added since the last update
     * @param deletedEntries Tags which have been deleted since the last update
     */
    public void setTrackTags(String trackUri, ArrayList<Tag> addedEntries, ArrayList<Tag> deletedEntries) {
        if(!open) return;

        for(Tag t : addedEntries) {
            int tagKey = lookupTagKey(t);
            Log.d(TAG, "Adding tag " + t.name + " to track " + trackUri);
            addTrackTag(trackUri, tagKey);
        }

        for(Tag t : deletedEntries) {
            int tagKey = lookupTagKey(t);
            Log.d(TAG, "Deleting tag " + t.name + " from track " + trackUri);
            deleteTrackTag(trackUri, tagKey);
        }
    }

    /**
     * Adds a new entry in the database linking a tag to a track.
     * @param trackUri Track uri
     * @param tagKey PK of tag entry in 'tags' table
     */
    private void addTrackTag(String trackUri, int tagKey) {
        if(!open) return;

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
        if(!open) return;

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

    /**
     * Queries the database to get a list of all entries in a column.
     * @param tableName Table to query on
     * @param colName Column to retrieve
     * @param groupByColumn Column to group by (null for no grouping)
     * @param sortByColumn Column to sort by (null for no sorting)
     * @return Contents of column in an array list
     */
    public ArrayList<String> getAllFromColumn(
            String tableName, String colName, String groupByColumn, String sortByColumn) {
        if(!open) return new ArrayList<>();

        Cursor results = database.query(
                tableName,
                new String[] { colName },
                null, null,
                groupByColumn,
                null,
                sortByColumn == null ? null : sortByColumn + " ASC"
        );
        results.moveToFirst();

        ArrayList<String> data = new ArrayList<>();
        while(!results.isAfterLast()) {
            data.add(results.getString(0));
            results.moveToNext();
        }
        results.close();
        return data;
    }

}
