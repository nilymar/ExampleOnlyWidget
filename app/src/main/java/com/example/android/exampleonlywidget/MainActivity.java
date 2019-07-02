package com.example.android.exampleonlywidget;

import android.app.LoaderManager;
import android.appwidget.AppWidgetManager;
import androidx.appcompat.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Weather>> {
    // Tag for the log messages
    public static final String LOG_TAG = MainActivity.class.getName();
    // base URL for the query the weather api (APIXU)
    private static final String APIXU_REQUEST_URL = "https://api.apixu.com/v1/forecast.json?";
    // Constant value for the weather loader ID
    private static final int WEATHER_LOADER_ID = 1;
    // Binding the views from the layout file (activity_main.xml) using ButterKnife
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.forecast_city)
    TextView forecastCity;
    @BindView(R.id.list)
    RecyclerView listView;
    @BindView(R.id.loading_spinner)
    ProgressBar progressBar;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mySwipeRefreshLayout;
    private LoaderManager loaderManager; // loadManager to be used in this activity
    private static final String SHARED_PREFERENCES = "weatherwidgetshared"; // name for sharedPreferences location
    // Adapter for the forecast weather days  (i.e. items in the array)
    private WeatherAdapter mAdapter;
    public ArrayList<Weather> weathers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        // set a new adapter for the list
        mAdapter = new WeatherAdapter(getApplicationContext(), weathers);
        // vertical RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        // set the adapter on the listView (this time - recyclerView)
        listView.setAdapter(mAdapter);
        // get a reference to the LoaderManager, in order to interact with loaders
        loaderManager = getLoaderManager();
        mySwipeRefreshLayout.setRefreshing(false); // set the status of swipe refreshing to false
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() { // this create a new list of forecast dates when you refresh
                        refreshData(); // using the method to reload data from the internet
                    }
                }
        );
        requestOperation(); // requesting data from the weather api for the first time
    }

    // method for re-loading the data from the internet
    private void refreshData() {
        loaderManager.destroyLoader(WEATHER_LOADER_ID); // so that the list will re-create
        weathers.clear(); // clearing current list from the adapter
        listView.removeAllViews(); // removing the items from the recyclerView
        requestOperation(); // requesting data from the internet
        mySwipeRefreshLayout.setRefreshing(false); // make sure the refresh spinner disappears when using with swipeRefresh
    }

    // this method checks if the internet is on - if so - starts the loader
    private void requestOperation() {
        // get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        if (!isConnected) { // if there is no internet connection - don't show progress bar and set
            // the no_internet_connection message
            progressBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(R.string.no_internet_connection);
        } else {
            emptyView.setVisibility(View.GONE);
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(WEATHER_LOADER_ID, null, this);
        }
    }

    // the loader constructor - creates the uri according to user preferences - your api key, the city and the number of days for forecast
    @Override
    public Loader<List<Weather>> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String queryCity = sharedPrefs.getString(getString(R.string.settings_city_key),
                getString(R.string.settings_city_default));
        String forecastDays = restorePreferences(getString(R.string.settings_forecast_days_key));
        if (forecastDays.isEmpty())
            forecastDays = getString(R.string.settings_forecast_days_default);
        Uri baseUri = Uri.parse(APIXU_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(getString(R.string.query_by_city), queryCity);
        uriBuilder.appendQueryParameter(getString(R.string.query_forecast_days), forecastDays);
        uriBuilder.appendQueryParameter("key", getResources().getString(R.string.api_key));
        Log.i(LOG_TAG, uriBuilder.toString());
        return new WeatherLoader(this, uriBuilder.toString());
    }

    // This method to restore the custom preferences data
    public String restorePreferences(String key) {
        SharedPreferences myPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if (myPreferences.contains(key))
            return myPreferences.getString(key, "");
        else return "";
    }

    // what happens when the loading finished
    @Override
    public void onLoadFinished(Loader<List<Weather>> loader, List<Weather> loadedWeathers) {
        // Clear the adapter of previous weathers
        this.weathers.clear();
        listView.removeAllViews();
        // after loading is over - don't show the progress indicator
        progressBar.setVisibility(View.GONE);
        // if there is a valid list of {@link Weather}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (loadedWeathers != null && !loadedWeathers.isEmpty()) {
            this.weathers.addAll(loadedWeathers);
            mAdapter.notifyDataSetChanged();
            // set the city name as title before the recylerView list
            String forecastTitle = getString(R.string.forecast_title) + this.weathers.get(0).getCity();
            forecastCity.setText(forecastTitle);
            updateWeatherWidget();
        } else {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(R.string.no_data_available);
        }
    }

    // updating the widget with the loaded data
    private void updateWeatherWidget() {
        if (weathers == null || weathers.isEmpty()) {
            Log.i(LOG_TAG, "updateWeatherWidget activated weathers null");
            return;
        } else {
            Log.i(LOG_TAG, "updateWeatherWidget activated weathers not empty");
            Weather weather = weathers.get(0);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            // update the weather widgets
            WeatherWidgetProvider.updateWeatherWidgets(this, AppWidgetManager.getInstance(this),
                    weather, appWidgetManager.getAppWidgetIds
                            (new ComponentName(this, WeatherWidgetProvider.class)));
        }
    }

    // what to do on loader reset
    @Override
    public void onLoaderReset(Loader<List<Weather>> loader) {
        weathers.clear();
    }

    // inflating the menu file
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // setting the options for actions in the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                // start SettingActivity file when user click on settings
                Intent settingsIntent = new Intent(this, SettingActivity.class);
                startActivity(settingsIntent);
                return true;
            // inflate about window when the user click on about
            case R.id.action_about:
                showAbout();
        }
        return super.onOptionsItemSelected(item);
    }

    // creating the window with the about screen (credits) for the app (option in the navigation drawer menu)
    protected void showAbout() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.about_window, null, false);
        TextView textView = messageView.findViewById(R.id.about_credits);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }


}
