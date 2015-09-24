package com.kwohlford.smartplaylistmanager.tracklist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.kwohlford.smartplaylistmanager.R;
import com.kwohlford.smartplaylistmanager.util.Config;

import java.util.ArrayList;

/**
 * Dialog-style screen for adding, renaming, and deleting tags.
 */
public class EditTagsActivity extends Activity {

    // Keys for intent extras
    public static final String
            KEY_TAGLIST = "tags",
            KEY_TYPE = "type";

    // Category of tags being edited
    public Tag.TagType type;

    // Layout & views
    public ListView tagList;
    public TagListAdapter tagListAdapter;

    // List of changes to return on activity result
    private ArrayList<Tag> changedTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tags);

        // Load data from extras
        Bundle extras = getIntent().getExtras();
        type = Tag.TagType.getTypeforId(extras.getInt(KEY_TYPE));
        setTitle(type == Tag.TagType.GENRE ?
                R.string.title_activity_editgenres
                : R.string.title_activity_editmoods);
        ArrayList<Tag> tags = extras.getParcelableArrayList(KEY_TAGLIST);

        // Set up tag list
        tagList = (ListView) findViewById(R.id.tagList);
        tagListAdapter = new TagListAdapter(tags);
        tagList.setAdapter(tagListAdapter);

        // Set up result intent
        changedTags = new ArrayList<>();
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(KEY_TAGLIST, changedTags);
        setResult(Config.REQCODE_EDITTAGS, intent);
    }

    /**
     * Creates dialog for renaming a tag.
     * @param view Clicked view
     */
    public void editTag(View view) {
        final Tag tag = (Tag) ((LinearLayout) view.getParent()).getTag();
        final EditText tagName = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        tagName.setLayoutParams(lp);

        new AlertDialog.Builder(this)
                .setTitle("Rename tag")
                .setMessage("Enter new name for \"" + tag.name + "\":")
                .setView(tagName)
                .setIcon(R.drawable.tag_outline)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        saveEditedTag(tag, tagName.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }


    /**
     * Creates dialog for deleting a tag.
     * @param view Clicked view
     */
    public void deleteTag(View view) {
        final Tag tag = (Tag) ((LinearLayout) view.getParent()).getTag();

        new AlertDialog.Builder(this)
                .setTitle("Delete tag")
                .setMessage("Are you sure you want to delete the tag \""
                        + tag.name + "\"? This cannot be undone!")
                .setIcon(R.drawable.tag_outline)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        saveDeletedTag(tag);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }


    /**
     * Creates dialog for adding a new tag.
     * @param view Clicked view
     */
    public void addTag(View view) {
        final EditText tagName = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        tagName.setLayoutParams(lp);

        new AlertDialog.Builder(this)
                .setTitle("New tag")
                .setView(tagName)
                .setIcon(R.drawable.tag_outline)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        saveAddedTag(tagName.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    /**
     * Creates a new tag and updates the tag list.
     * @param name Name of new tag
     */
    private void saveAddedTag(String name) {
        Tag added = new Tag(name, type, Tag.FLAG_ADDED, "");
        changedTags.add(added);
        tagListAdapter.addTag(added);
    }

    /**
     * Renames a tag and updates the tag list.
     * @param oldTag Tag to edit
     * @param newName New name for tag
     */
    private void saveEditedTag(Tag oldTag, String newName) {
        Tag edited = new Tag(newName, type, Tag.FLAG_CHANGED, oldTag.name);
        changedTags.add(edited);
        tagListAdapter.changeTag(oldTag, edited);
    }

    /**
     * Flags a tag for deletion and updates the tag list.
     * @param deleted Tag to delete
     */
    private void saveDeletedTag(Tag deleted) {
        tagListAdapter.deleteTag(deleted);
        deleted.changeFlag = Tag.FLAG_DELETED;
        changedTags.add(deleted);
    }

    /**
     * Close the activity without saving.
     * @param view View clicked
     */
    public void closeCancel(View view) {
        changedTags = new ArrayList<>();
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(KEY_TAGLIST, changedTags);
        setResult(Config.REQCODE_EDITTAGS, intent);
        finish();
    }

    /**
     * Save and quit the activity.
     * @param view View clicked
     */
    public void closeSave(View view) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(KEY_TAGLIST, changedTags);
        setResult(Config.REQCODE_EDITTAGS, intent);
        finish();
    }
}
