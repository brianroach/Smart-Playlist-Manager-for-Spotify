package com.kwohlford.smartplaylistmanager;

/**
 * Container for storing tags and related information.
 */
public class Tag {

    public String name;
    public TagType type;

    /* Possible tag categories */
    public enum TagType {
        GENRE(1), MOOD(2);
        public final int id;
        TagType(int id) { this.id = id; }
    }

    public Tag(String name, TagType type) {
        this.name = name;
        this.type = type;
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
}
