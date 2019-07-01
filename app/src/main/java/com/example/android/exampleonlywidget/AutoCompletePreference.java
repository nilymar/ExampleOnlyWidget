package com.example.android.exampleonlywidget;

import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class AutoCompletePreference extends EditTextPreference {
    public AutoCompletePreference(Context context) {
        super(context);
    }

    public AutoCompletePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompletePreference(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * the default EditTextPreference does not make it easy to
     * use an AutoCompleteEditTextPreference field. By overriding this method
     * we perform surgery on it to use the type of edit field that
     * we want.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        // find the current EditText object
        final EditText editText = (EditText) view.findViewById(android.R.id.edit);
        // copy its layout params
        ViewGroup.LayoutParams params = editText.getLayoutParams();
        ViewGroup vg = (ViewGroup) editText.getParent();
        String curVal = editText.getText().toString();
        // remove it from the existing layout hierarchy
        vg.removeView(editText);
        // construct a new editable autocomplete object with the appropriate params
        // and id that the TextEditPreference is expecting
        mACTV = new AutoCompleteTextView(getContext());
        mACTV.setLayoutParams(params);
        mACTV.setId(android.R.id.edit);
        mACTV.setText(curVal);
        mACTV.setSelectAllOnFocus(true);
        String[] PLACES = new String[0];
        JSONArray jsonLocations = null;
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            jsonLocations = obj.getJSONArray("locations");

            } catch (JSONException e1) {
            e1.printStackTrace();
        }
        if (jsonLocations!=null) {
            PLACES = new String[jsonLocations.length()];
            for (int i = 0, count = jsonLocations.length(); i < count; i++) {
                try {
                    String jsonString = jsonLocations.getString(i);
                    PLACES[i] = jsonString;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }    

        if (PLACES.length!=0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, PLACES);
            mACTV.setAdapter(adapter);
        }

        // add the new view to the layout
        vg.addView(mACTV);
    }

    private String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getContext().getAssets().open("locations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Because the baseclass does not handle this correctly
     * we need to query our injected AutoCompleteTextView for
     * the value to save
     */
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);

        if (positiveResult && mACTV != null)
        {
            String value = mACTV.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    /**
     * again we need to override methods from the base class
     */
    public EditText getEditText()
    {
        return mACTV;
    }

    private AutoCompleteTextView mACTV = null;
    private final String TAG = "AutoCompleteEditTextPreference";
}