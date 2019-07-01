package com.example.android.exampleonlywidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import static com.example.android.exampleonlywidget.WidgetUpdateJobIntentService.ACTION_UPDATE_WEATHER_WIDGET_ONLINE;


// with this we create the setting screen, accessible from the menu,
// where you can set the city and number of days for the weather forecast
public class SettingActivity extends AppCompatActivity {
    private static final String SHARED_PREFERENCES = "weatherwidgetshared"; // name for sharedPreferences location
    long appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        /// this is for when the setting screen is accessed from the widget installation
        setResult(RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            ActionBar actionBar = getSupportActionBar();
            // hide the home (back to mainActivity) button when the setting screen pop on new widget installment
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(false); // disable the button
                actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
                actionBar.setDisplayShowHomeEnabled(false); // remove the icon
            }
        }
    }

    public static class WeatherPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

//        @Override
//        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//            super.onCreate(savedInstanceState);
//            addPreferencesFromResource(R.xml.setting_main);
//            Preference setForcastLocation = findPreference(getString(R.string.settings_city_key));
//            bindPreferenceSummaryToValue(setForcastLocation);
//            Preference setForecastDays = findPreference(getString(R.string.settings_forecast_days_key));
//            bindPreferenceSummaryToValue(setForecastDays);
//        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.setting_main);
            Preference setForcastLocation = findPreference(getString(R.string.settings_city_key));
            bindPreferenceSummaryToValue(setForcastLocation);
            Preference setForecastDays = findPreference(getString(R.string.settings_forecast_days_key));
            bindPreferenceSummaryToValue(setForecastDays);
        }


        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof AutoCompletePreference) {
                if (value.toString().isEmpty())
                    preference.setSummary(getString(R.string.settings_city_default)); // if there was no city saved, that the default city
            //}
//            if (preference instanceof EditTextPreference) {
//                if (value.toString().isEmpty())
//                    preference.setSummary(getString(R.string.settings_city_default)); // if there was no city saved, that the default city
            } else if (preference instanceof NumberPickerPreference) {
                NumberPickerPreference numPref = (NumberPickerPreference) preference;
                String forecastDays = String.valueOf(numPref.getValue());
                savePreferences(preference.getKey(), forecastDays);
                preference.setSummary(forecastDays);

            }
            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            if (preference instanceof AutoCompletePreference) { // for the city
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                String preferenceString = preferences.getString(preference.getKey(), "");
                onPreferenceChange(preference, preferenceString);
//            }
//            if (preference instanceof EditTextPreference) { // for the city
//                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
//                String preferenceString = preferences.getString(preference.getKey(), "");
//                onPreferenceChange(preference, preferenceString);
            } else if (preference instanceof NumberPickerPreference){
                String preferenceString = restorePreferences(preference.getKey());
                onPreferenceChange(preference, preferenceString);
            }
        }

        // This method to store the custom preferences changes
        public void savePreferences(String key, String value) {
            SharedPreferences myPreferences = this.getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor myEditor = myPreferences.edit();
            myEditor.putString(key, value);
            myEditor.apply();
        }

        // This method to restore the custom preferences data
        public String restorePreferences(String key) {
            SharedPreferences myPreferences = this.getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
            if (myPreferences.contains(key))
                return myPreferences.getString(key, "");
            else return "";
        }
    }

    @Override
    public void onBackPressed() {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            // didn't come from the widget - so go back to where it is defined - i.e. mainActivity
            NavUtils.navigateUpFromSameTask(this);
        else {
            // It is the responsibility of the configuration activity to update the app widget
            ServiceResultReceiver mServiceResultReceiver = new ServiceResultReceiver(new Handler());
            WidgetUpdateJobIntentService.enqueueWork(this, mServiceResultReceiver,
                    ACTION_UPDATE_WEATHER_WIDGET_ONLINE);
            // need to sent the widget id as result intent
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            // instead of going to mainActivity - close the app
            finish();
        }
    }
}

