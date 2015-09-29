package com.kwohlford.smartplaylistmanager.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kwohlford.smartplaylistmanager.R;

import java.util.ArrayList;

/**
 * Created by Kirsten on 25.09.2015.
 */
public class CriteriaListAdapter extends BaseAdapter {
    private ArrayList<Criteria> dataset;

    public CriteriaListAdapter(ArrayList<Criteria> dataset) {
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
                    .inflate(R.layout.playlist_criteria_item, viewGroup, false);
        }
        return view;
    }
}
