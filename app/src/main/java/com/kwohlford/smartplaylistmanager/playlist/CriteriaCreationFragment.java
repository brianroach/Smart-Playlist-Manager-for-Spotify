package com.kwohlford.smartplaylistmanager.playlist;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.kwohlford.smartplaylistmanager.R;
import com.kwohlford.smartplaylistmanager.db.SourceTrackData;
import com.kwohlford.smartplaylistmanager.tracklist.Tag;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kirsten on 25.09.2015.
 */
public class CriteriaCreationFragment extends DialogFragment {

    // Inner views
    private RadioButton radioInclude;
    private Spinner spinCriteria;
    private Spinner spinComparator;
    private RatingBar ratingSingle;
    private RatingBar ratingDouble;
    private TextView txtRatingDouble;
    private Spinner spinnerSingle;
    private Spinner spinnerMultiple;
    private EditText txtboxContains;

    private CriteriaType selectedType;
    private ComparativeClause selectedComparator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playlist_criteria_item, container, false);
        getDialog().setTitle(R.string.txt_createfilter);

        // Get view references
        radioInclude = (RadioButton) rootView.findViewById(R.id.radioInclude);
        spinCriteria = (Spinner) rootView.findViewById(R.id.spinCriteria);
        spinComparator = (Spinner) rootView.findViewById(R.id.spinComparator);
        ratingSingle = (RatingBar) rootView.findViewById(R.id.ratingSingle);
        ratingDouble = (RatingBar) rootView.findViewById(R.id.ratingDouble);
        txtRatingDouble = (TextView) rootView.findViewById(R.id.txtRatingDouble);
        spinnerSingle = (Spinner) rootView.findViewById(R.id.spinSelectSingle);
        spinnerMultiple = (Spinner) rootView.findViewById(R.id.spinSelectMultiple);
        txtboxContains = (EditText) rootView.findViewById(R.id.txtboxContains);

        // Set up views
        setCritTypeSpinner(CriteriaType.values(), rootView.getContext());
        rootView.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreatePlaylistActivity callingActivity = (CreatePlaylistActivity) getActivity();
                callingActivity.addCriteria(new Criteria(radioInclude.isChecked(), selectedType, selectedComparator));
                getDialog().dismiss();
            }
        });

        rootView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        ratingSingle.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                if(ratingDouble.getRating() < ratingSingle.getRating()) {
                    ratingDouble.setRating(ratingSingle.getRating());
                }
            }
        });

        return rootView;
    }

    public void setCritTypeSpinner(final CriteriaType[] items, final Context context) {
        String[] display = CriteriaType.getStringValues(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, display);
        spinCriteria.setAdapter(adapter);
        spinCriteria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinComparator.setEnabled(true);
                selectedType = items[i];
                setComparatorSpinner(selectedType, context);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinComparator.setEnabled(false);
                spinComparator.setAdapter(
                        new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item));
                enableInputView(InputType.NONE);
            }
        });
    }

    public void setComparatorSpinner(final CriteriaType type, final Context context) {
        final ComparativeClause[] items = CriteriaType.getValidComparators(type);
        String[] display = ComparativeClause.getStringValues(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, display);
        spinComparator.setAdapter(adapter);
        spinComparator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedComparator = items[i];
                enableInputView(ComparativeClause.getInputType(type, selectedComparator));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                enableInputView(InputType.NONE);
            }
        });
    }

    private void enableInputView(InputType type) {
        ratingSingle.setVisibility(View.GONE);
        ratingDouble.setVisibility(View.GONE);
        txtRatingDouble.setVisibility(View.GONE);
        spinnerSingle.setVisibility(View.GONE);
        spinnerMultiple.setVisibility(View.GONE);
        txtboxContains.setVisibility(View.GONE);
        switch (type) {
            case NONE:
                break;
            case RATING_SINGLE:
                ratingSingle.setVisibility(View.VISIBLE);
                break;
            case RATING_DOUBLE:
                ratingSingle.setVisibility(View.VISIBLE);
                txtRatingDouble.setVisibility(View.VISIBLE);
                ratingDouble.setVisibility(View.VISIBLE);
                break;
            case SPINNER_SINGLE:
                spinnerSingle.setVisibility(View.VISIBLE);
                break;
            case SPINNER_MULTIPLE:
                spinnerMultiple.setVisibility(View.VISIBLE);
                break;
            case FREETEXT:
                txtboxContains.setVisibility(View.VISIBLE);
                txtboxContains.setText("");
                txtboxContains.setHint("(enter filter text)");
                break;
        }
    }

}