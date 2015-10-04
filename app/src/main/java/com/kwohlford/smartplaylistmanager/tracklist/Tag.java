package com.kwohlford.smartplaylistmanager.tracklist;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Container for storing tags and related information.
 */
public class Tag implements Parcelable {

    public String name;
    public TagType type;

    /* Possible tag categories */
    public enum TagType {
        GENRE(1), MOOD(2);
        public final int id;
        TagType(int id) { this.id = id; }
        public static TagType getTypeforId(int id) {
            if(id == 1) return GENRE;
            else return MOOD;
        }
    }

    /**
     * Create a new tag.
     * @param name Display name
     * @param type Category
     */
    public Tag(String name, TagType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Create tag from a parcel.
     * @param in Parcel
     */
    protected Tag(Parcel in) {
        this(
            in.readString(),
            TagType.getTypeforId(in.readInt())
        );
    }

    @Override
    public int hashCode() {
        return type.hashCode() + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tag))
            return false;
        if (obj == this)
            return true;

        Tag t = (Tag) obj;
        return type.equals(t.type) && name.equals(t.name);
    }

    @Override
    public String toString() {
        return name;
    }

    public static String tagListToString(ArrayList<Tag> tags) {
        StringBuilder s = new StringBuilder();
        boolean hasTags = false;
        for(Tag tag : tags) {
            hasTags = true;
            s.append(tag).append(", ");
        }
        if(hasTags) {
            s.deleteCharAt(s.lastIndexOf(","));
        } else {
            s.append("none");
        }
        return s.toString();
    }

    /** Begin parcelable inherited methods **/
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeInt(type.id);
    }

    public static final Parcelable.Creator<Tag> CREATOR
            = new Parcelable.Creator<Tag>() {

        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
    /** End parcelable inherited methods **/

}
