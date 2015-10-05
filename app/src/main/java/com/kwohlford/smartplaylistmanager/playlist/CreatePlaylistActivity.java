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
    private CriteriaListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        // Set up criteria list
        criteriaListView = (ListView) findViewById(R.id.filterList);
        criteriaList = new ArrayList<>();
        adapter = new CriteriaListAdapter(criteriaList);
        criteriaListView.setAdapter(adapter);
    }

    public void createCriteria(View view) {
        FragmentManager fm = getFragmentManager();
        CriteriaCreationFragment dialogFragment = new CriteriaCreationFragment();
        dialogFragment.show(fm, "Create filter");
    }

    public void addCriteria(Criteria criteria) {
        adapter.addCriteria(criteria);
    }

    public void closeCancel(View view) {
        finish();
    }

    public void closeContinue(View view) {
        // todo
        finish();
    }

    public void editFilter(View view) {
        FragmentManager fm = getFragmentManager();
        CriteriaCreationFragment dialogFragment = new CriteriaCreationFragment();
        // todo
        dialogFragment.show(fm, "Create filter");
    }

    public void deleteFilter(View view) {
    }
}
