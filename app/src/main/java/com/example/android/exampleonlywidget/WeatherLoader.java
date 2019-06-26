package com.example.android.exampleonlywidget;

import android.content.AsyncTaskLoader;
import android.content.Context;
import java.util.List;

public class WeatherLoader extends AsyncTaskLoader<List<Weather>> {
    // Query URL
    private String mUrl;
    // the List variable for Weather objects
    private List<Weather> weathers;

    /**
     * Constructs a new {@link WeatherLoader}.
     * @param context of the activity
     * @param url     to load data from
     */
    public WeatherLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        if (weathers != null) {// if you have weathers in the list - use that list
            // Use cached data
            deliverResult(weathers);
        }
        else  {
            forceLoad();
        }
    }

    // This is on a background thread.
    @Override
    public List<Weather> loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        weathers = QueryUtils.fetchWeathers(mUrl);
        return weathers;
    }

    @Override
    public void deliverResult(List<Weather> data) {
        // We’ll save the data for later retrieval
        weathers = data;
        super.deliverResult(data);
    }

}
