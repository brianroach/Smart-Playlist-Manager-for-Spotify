package com.kwohlford.smartplaylistmanager.playlist;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.kwohlford.smartplaylistmanager.R;
import com.kwohlford.smartplaylistmanager.db.SourceTrackData;
import com.kwohlford.smartplaylistmanager.db.TableTracks;
import com.kwohlford.smartplaylistmanager.playlist.criteria.ComparativeClause;
import com.kwohlford.smartplaylistmanager.playlist.criteria.Criteria;
import com.kwohlford.smartplaylistmanager.playlist.criteria.CriteriaInput;
import com.kwohlford.smartplaylistmanager.playlist.criteria.CriteriaType;
import com.kwohlford.smartplaylistmanager.playlist.criteria.InputFloat;
import com.kwohlford.smartplaylistmanager.playlist.criteria.InputString;
import com.kwohlford.smartplaylistmanager.playlist.criteria.InputTag;
import com.kwohlford.smartplaylistmanager.tracklist.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * DialogFragment allowing user to define a new criteria for playlist generation.
 */
public class CriteriaCreationFragment extends DialogFragment {

    // Views
    private Context context;
    private RadioButton radioInclude;
    private Spinner spinCriteria;
    private Spinner spinComparator;
    private RatingBar ratingSingle;
    private RatingBar ratingDouble;
    private TextView txtRatingDouble;
    private Spinner spinnerSingle;
    private Spinner spinnerMultiple;
    private LinearLayout layoutInput;

    // Criteria being edited
    private Criteria criteria;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playlist_criteria_item, container, false);
        getDialog().setTitle(R.string.txt_createfilter);

        criteria = new Criteria();

        // Get view references
        context = rootView.getContext();
        radioInclude = (RadioButton) rootView.findViewById(R.id.radioInclude);
        spinCriteria = (Spinner) rootView.findViewById(R.id.spinCriteria);
        spinComparator = (Spinner) rootView.findViewById(R.id.spinComparator);
        ratingSingle = (RatingBar) rootView.findViewById(R.id.ratingSingle);
        ratingDouble = (RatingBar) rootView.findViewById(R.id.ratingDouble);
        txtRatingDouble = (TextView) rootView.findViewById(R.id.txtRatingDouble);
        spinnerSingle = (Spinner) rootView.findViewById(R.id.spinSelectSingle);
        spinnerMultiple = (Spinner) rootView.findViewById(R.id.spinSelectMultiple);
        layoutInput = (LinearLayout) rootView.findViewById(R.id.layoutInput);

        // Set up views and buttons
        setCritTypeSpinner();
        rootView.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreatePlaylistActivity callingActivity = (CreatePlaylistActivity) getActivity();
                criteria.include = radioInclude.isChecked();
                callingActivity.addCriteria(criteria);
                getDialog().dismiss();
            }
        });
        rootView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        return rootView;
    }

    /**
     * Sets up spinner for selecting criteria type.
     */
    public void setCritTypeSpinner() {
        final CriteriaType[] items = CriteriaType.values();
        String[] display = CriteriaType.getStringValues(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_selectable_list_item, display);
        spinCriteria.setAdapter(adapter);
        spinCriteria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinComparator.setEnabled(true);
                criteria.type = items[i];
                setComparatorSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spinComparator.setEnabled(false);
                spinComparator.setAdapter(
                        new ArrayAdapter<String>(context, android.R.layout.simple_selectable_list_item));
                enableInputView();
            }
        });
    }

    /**
     * Sets up spinner for selecting criteria comparator.
     */
    public void setComparatorSpinner() {
        final ComparativeClause[] items = CriteriaType.getValidComparators(criteria.type);
        String[] display = ComparativeClause.getStringValues(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_selectable_list_item, display);
        spinComparator.setAdapter(adapter);
        spinComparator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                criteria.comparator = items[i];
                enableInputView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                enableInputView();
            }
        });
    }

    /**
     * Determines which input view to display based on the currently selected criteria and populates
     * it with the correct values.
     */
    private void enableInputView() {
        resetInputViews();

        switch(criteria.type) {
            case RATING:
                switch(criteria.comparator) {
                    case EQUALS: case NOT_EQ:
                    case GREATER: case GREATER_EQ:
                    case LESS: case LESS_EQ:
                        ratingSingle.setVisibility(View.VISIBLE);
                        createRatingBar(0f);
                        break;
                    case BETWEEN:
                        ratingSingle.setVisibility(View.VISIBLE);
                        txtRatingDouble.setVisibility(View.VISIBLE);
                        ratingDouble.setVisibility(View.VISIBLE);
                        createRatingBar(0f, 0f);
                        break;
                }
                break;
            case GENRE: case MOOD:
                ArrayList<Tag> tags = SourceTrackData.getInstance().tracks.getTags(
                        criteria.type == CriteriaType.GENRE ? Tag.TagType.GENRE : Tag.TagType.MOOD);
                String[] tagArr = new String[tags.size()];
                for(int i = 0; i < tagArr.length; i++ ) {
                    tagArr[i] = tags.get(i).name;
                }
                switch(criteria.comparator) {
                    case EQUALS: case NOT_EQ:
                        spinnerSingle.setVisibility(View.VISIBLE);
                        createInputSpinner(tags, tagArr, new InputTag(), 0);
                        break;
                    case IN: case NOT_IN:
                        spinnerMultiple.setVisibility(View.VISIBLE);
                        boolean[] state = new boolean[tagArr.length];
                        Arrays.fill(state, false);
                        createInputSpinner(tags, tagArr, new InputTag(), state);
                        break;
                }
                break;
            case ARTIST: case ALBUM:
                String col = criteria.type == CriteriaType.ARTIST ? TableTracks.COL_ARTIST : TableTracks.COL_ALBUM;
                ArrayList<String> possibleValuess = SourceTrackData.getInstance()
                        .getAllFromColumn(TableTracks.NAME, col, col, col);
                String[] valueArr = possibleValuess.toArray(new String[possibleValuess.size()]);
                switch(criteria.comparator) {
                    case EQUALS: case NOT_EQ:
                        spinnerSingle.setVisibility(View.VISIBLE);
                        createInputSpinner(possibleValuess, valueArr, new InputString(), 0);
                        break;
                    case IN: case NOT_IN:
                        spinnerMultiple.setVisibility(View.VISIBLE);
                        boolean[] state = new boolean[valueArr.length];
                        Arrays.fill(state, false);
                        createInputSpinner(possibleValuess, valueArr, new InputString(), state);
                        break;
                }
                break;
        }
    }

    /**
     * Resets all input views to default state and hides them from view.
     */
    private void resetInputViews() {
        criteria.inputtedValue = null;
        layoutInput.setOnClickListener(null);

        ratingSingle.setVisibility(View.GONE);
        ratingSingle.setRating(0);

        txtRatingDouble.setVisibility(View.GONE);
        ratingDouble.setVisibility(View.GONE);
        ratingDouble.setRating(0);

        spinnerSingle.setVisibility(View.GONE);
        spinnerSingle.setAdapter(null);

        spinnerMultiple.setVisibility(View.GONE);
        spinnerMultiple.setAdapter(null);
    }

    /**
     * Sets the criteria input to a single float and listens for changes on the rating bar.
     * @param startingValue Initial rating value, from 0 to 5
     */
    private void createRatingBar(float startingValue) {
        final InputFloat input = new InputFloat();
        criteria.inputtedValue = input;
        ratingSingle.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                input.setInput(new ArrayList<>(Collections.singletonList(v)));
            }
        });
        ratingSingle.setRating(startingValue);
    }

    /**
     * Sets the criteria input to two floats and listens for changes on two rating bars.
     * @param startingMin Initial rating value of first rating bar, from 0 to 5
     * @param startingMax Initial rating value of second rating bar, from 0 to 5
     */
    private void createRatingBar(float startingMin, float startingMax) {
        final InputFloat input = new InputFloat();
        criteria.inputtedValue = input;

        ratingSingle.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                if(ratingDouble.getRating() < v) {
                    ratingDouble.setRating(v);
                }
                input.setInput(new ArrayList<>(Arrays.asList(v, ratingDouble.getRating())));
            }
        });
        ratingSingle.setRating(startingMin);

        ratingDouble.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                if (v < ratingSingle.getRating()) {
                    ratingSingle.setRating(v);
                }
                input.setInput(new ArrayList<>(Arrays.asList(ratingSingle.getRating(), v)));
            }
        });
        ratingDouble.setRating(startingMax);
    }

    /**
     * Sets the criteria input to get a value from a single-selection spinner.
     * @param inputValues List of all possible input values
     * @param valuesAsStrings String representation of possible input values
     * @param criteriaInput Input container to be updated
     * @param indexSelected Index of value to set as initially selected
     * @param <T> Type of input
     */
    private <T> void createInputSpinner(
            final ArrayList<T> inputValues,
            final String[] valuesAsStrings,
            final CriteriaInput<T> criteriaInput,
            final int indexSelected) {
        criteria.inputtedValue = criteriaInput;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_selectable_list_item, valuesAsStrings);
        spinnerSingle.setAdapter(adapter);
        spinnerSingle.setSelection(indexSelected);
        spinnerSingle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                criteriaInput.setInput(new ArrayList<>(Collections.singletonList(inputValues.get(i))));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                criteriaInput.setInput(new ArrayList<T>());
            }
        });
    }

    /**
     * Sets the criteria input to get a value from a multiple-selection spinner.
     * @param inputValues List of all possible input values
     * @param valuesAsStrings String representation of possible input values
     * @param criteriaInput Input container to be updated
     * @param checkedState Boolean array specifying which entries to set as initially selected
     * @param <T> Type of input
     */
    private <T> void createInputSpinner(
            final ArrayList<T> inputValues,
            final String[] valuesAsStrings,
            final CriteriaInput<T> criteriaInput,
            final boolean[] checkedState) {
        criteria.inputtedValue = criteriaInput;
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setMultiChoiceItems(valuesAsStrings, checkedState, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index, boolean isChecked) {
                        checkedState[index] = isChecked;
                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        ArrayList<T> selected = new ArrayList<>();
                        for (int i = 0; i < valuesAsStrings.length; i++) {
                            if (checkedState[i]) selected.add(inputValues.get(i));
                        }
                        criteriaInput.setInput(selected);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                context, android.R.layout.simple_selectable_list_item,
                                new String[]{ criteriaInput.toString() });
                        spinnerMultiple.setAdapter(adapter);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .create();
        layoutInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
    }

}