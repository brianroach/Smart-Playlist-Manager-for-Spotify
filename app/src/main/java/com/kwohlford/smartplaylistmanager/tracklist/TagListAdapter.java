package com.kwohlford.smartplaylistmanager.tracklist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kwohlford.smartplaylistmanager.R;

import java.util.ArrayList;

/**
 * Adapter for a ListView containing editable tag entries.
 */
public class TagListAdapter extends BaseAdapter {

    private ArrayList<Tag> dataset;

    public TagListAdapter(ArrayList<Tag> dataset) {
        this.dataset = dataset;
    }

    @Override
    public int getCount() {
        return dataset.size();
    }

    @Override
    public Object getItem(int i) {
        return dataset.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = LayoutInflater
                    .from(viewGroup.getContext())
                    .inflate(R.layout.tag_list_item, viewGroup, false);
        }
        TextView txtTagName = (TextView) view.findViewById(R.id.txtTagName);
        txtTagName.setText(dataset.get(i).name);
        view.setTag(dataset.get(i));
        return view;
    }

    /**
     * Add a tag to the dataset.
     * @param t Tag to add
     */
    public void addTag(Tag t) {
        dataset.add(t);
        notifyDataSetChanged();
    }

    /**
     * Rename a tag in the dataset.
     * @param oldT Tag to edit
     * @param newName New name of the tag
     */
    public void changeTag(Tag oldT, String newName) {
        dataset.get(dataset.indexOf(oldT)).name = newName;
        notifyDataSetChanged();
    }

    /**
     * Remove a tag from the dataset.
     * @param t Tag to delete
     */
    public void deleteTag(Tag t) {
        dataset.remove(t);
        notifyDataSetChanged();
    }

}
