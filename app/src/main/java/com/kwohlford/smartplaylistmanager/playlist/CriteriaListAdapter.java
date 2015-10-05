package com.kwohlford.smartplaylistmanager.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kwohlford.smartplaylistmanager.R;
import com.kwohlford.smartplaylistmanager.playlist.criteria.Criteria;

import java.util.ArrayList;

/**
 * Adapter for a ListView containing editable criteria entries.
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
        if (view == null) {
            view = LayoutInflater
                    .from(viewGroup.getContext())
                    .inflate(R.layout.criteria_list_item, viewGroup, false);
        }
        TextView txtCriteria = (TextView) view.findViewById(R.id.txtFilterDesc);
        txtCriteria.setText(dataset.get(i).toString());
        ImageView imgIncl = (ImageView) view.findViewById(R.id.imgInclExcl);
        imgIncl.setImageResource(dataset.get(i).include ?
                R.drawable.playlist_plus : R.drawable.playlist_remove);
        return view;
    }

    /**
     * Add a criteria item to the dataset.
     * @param c Criteria to add
     */
    public void addCriteria(Criteria c) {
        dataset.add(c);
        notifyDataSetChanged();
    }

    /**
     * Remove a criteria item from the dataset.
     * @param c Criteria to delete
     */
    public void deleteCriteria(Criteria c) {
        dataset.remove(c);
        notifyDataSetChanged();
    }

}
