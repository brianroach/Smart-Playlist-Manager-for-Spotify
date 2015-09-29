package com.kwohlford.smartplaylistmanager.playlist;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kwohlford.smartplaylistmanager.R;

import java.util.ArrayList;

public class CreatePlaylistActivity extends Activity {

    // Layout & views
    public ListView criteriaListView;
//    public CriteriaListAdapter criteriaListAdapter;
    public ArrayList<Criteria> criteriaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        // Set up criteriaList list
        criteriaListView = (ListView) findViewById(R.id.filterList);
        criteriaList = new ArrayList<>();
//        criteriaListAdapter = new CriteriaListAdapter(new ArrayList<Criteria>());
//        criteriaListView.setAdapter(criteriaListAdapter);

    }

    public void createCriteria(View view) {
        FragmentManager fm = getFragmentManager();
        CriteriaCreationFragment dialogFragment = new CriteriaCreationFragment();
        dialogFragment.show(fm, "Sample Fragment");
    }

    public void addCriteria(Criteria criteria) {
        criteriaList.add(criteria);
        String[] listItems = new String[criteriaList.size()];
        for(int i = 0; i < listItems.length; i++) {
            listItems[i] = criteriaList.get(i).toString();
        }

        criteriaListView.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, listItems));

    }

}
