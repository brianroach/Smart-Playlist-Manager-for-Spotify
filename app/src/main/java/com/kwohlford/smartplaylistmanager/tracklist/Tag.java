package com.kwohlford.smartplaylistmanager.tracklist;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Container for storing tags and related information.
 */
public class Tag implements Parcelable {

    public String name;
    public TagType type;

    /* Used to mark tag for update on next load */
    public int changeFlag;
    public String prevName;
    public static int
            FLAG_NONE = 0,
            FLAG_ADDED = 1,
            FLAG_DELETED = 2,
            FLAG_CHANGED = 3;

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
        changeFlag = FLAG_NONE;
        prevName = "";
    }

    /**
     * Create a new tag with additional options.
     * @param name Display name
     * @param type Category
     * @param changeFlag Integer flag used to mark tag for update
     * @param prevName If the tag was recently renamed, provide the old name, otherwise empty string
     */
    public Tag(String name, TagType type, int changeFlag, String prevName) {
        this.name = name;
        this.type = type;
        this.changeFlag = changeFlag;
        this.prevName = prevName;
    }

    /**
     * Create tag from a parcel.
     * @param in Parcel
     */
    protected Tag(Parcel in) {
        this(
            in.readString(),
            TagType.getTypeforId(in.readInt()),
            in.readInt(),
            in.readString()
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

    /** Begin parcelable inherited methods **/
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeInt(type.id);
        out.writeInt(changeFlag);
        out.writeString(prevName);
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
