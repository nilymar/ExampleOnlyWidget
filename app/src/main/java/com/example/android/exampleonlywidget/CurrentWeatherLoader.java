package com.example.android.exampleonlywidget;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

public class CurrentWeatherLoader extends AsyncTaskLoader<Weather> {
    // Tag for log messages
    private static final String LOG_TAG = CurrentWeatherLoader.class.getName();
    // Query URL
    private String mUrl;
    // the List variable for Weather objects
    private Weather weather;

    /**
     * Constructs a new {@link CurrentWeatherLoader}.
     * @param context of the activity
     * @param url     to load data from
     */
    public CurrentWeatherLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        if (weather != null) {// if you have weathers in the list - use that list
            // Use cached data
            deliverResult(weather);
        }
        else  {
            forceLoad();
        }
    }

    // This is on a background thread.
    @Override
    public Weather loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        List<Weather> weathers = QueryUtils.fetchWeathers(mUrl);
        return weathers.get(0);
    }

    @Override
    public void deliverResult(Weather data) {
        // We’ll save the data for later retrieval
        weather = data;
        super.deliverResult(data);
    }

}
