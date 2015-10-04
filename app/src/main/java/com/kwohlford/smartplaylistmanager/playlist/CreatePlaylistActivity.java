package com.kwohlford.smartplaylistmanager.playlist;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kwohlford.smartplaylistmanager.R;
import com.kwohlford.smartplaylistmanager.playlist.criteria.Criteria;

import java.util.ArrayList;

/**
 * Screen containing a list of user-defined criteria for generating a playlist.
 */
public class CreatePlaylistActivity extends Activity {

    // Layout & views
    public ListView criteriaListView;
    public ArrayList<Criteria> criteriaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        // Set up criteria list
        criteriaListView = (ListView) findViewById(R.id.filterList);
        criteriaList = new ArrayList<>();
    }

    public void createCriteria(View view) {
        FragmentManager fm = getFragmentManager();
        CriteriaCreationFragment dialogFragment = new CriteriaCreationFragment();
        dialogFragment.show(fm, "Create filter");
    }

    public void addCriteria(Criteria criteria) {
        // placeholder code for creating list
        criteriaList.add(criteria);
        String[] listItems = new String[criteriaList.size()];
        for(int i = 0; i < listItems.length; i++) {
            listItems[i] = criteriaList.get(i).toString();
        }
        criteriaListView.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_selectable_list_item, listItems));
    }

}
