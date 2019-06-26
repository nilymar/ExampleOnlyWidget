package com.example.android.exampleonlywidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

// A custom dialog preference that displays a number picker as a dialog.
public class NumberPickerPreference extends DialogPreference {
    private static final int MAX_VALUE = 7; // max value the user can choose
    private static final int MIN_VALUE = 1; // min value the user can choose
    private static final boolean WRAP_SELECTOR_WHEEL = false; // enable or disable the 'circular behavior' - set on false
    private NumberPicker picker; // this is for the NumberPicker view
    private int value; // this is for the value chosen / set in the NumberPicker
    private static final String SHARED_PREFERENCES = "weatherwidgetshared"; // name for sharedPreferences location

    // constructors for this preference
    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateDialogView() { // I used a number_picker.xml layout to create bigger margin between title and picker
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View dialogView = layoutInflater.inflate(R.layout.number_picker_layout, null);
        picker = (NumberPicker)dialogView.findViewById(R.id.number_picker);
        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        picker.setMinValue(MIN_VALUE); // setting the min value to display
        picker.setMaxValue(MAX_VALUE); // setting the max value to display
        picker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL); // either true or false
        picker.setValue(getValue());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            picker.clearFocus();
            int newValue = picker.getValue();
            if (callChangeListener(newValue)) {
                setValue(newValue); // when the dialog closed with ok - save the value
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, MIN_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(MIN_VALUE) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        this.value = value;
        persistInt(this.value);
        updateSummary(); // set the value in the summary view for the preference, in the correct style, and save in preference file
    }

    public int getValue() {
        return this.value;
    }

    private void updateSummary() {
        this.setSummary(String.valueOf(this.value));
        savePreferences(this.getKey(), String.valueOf(this.getValue())); // save the value when it is updated
    }
    // This method to save the custom preferences changes in shared preferences file
    private void savePreferences(String key, String value) {
        SharedPreferences myPreferences = this.getContext().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putString(key, value);
        myEditor.apply();
    }

}


