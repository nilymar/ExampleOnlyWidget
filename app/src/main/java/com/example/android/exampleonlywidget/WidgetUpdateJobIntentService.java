package com.example.android.exampleonlywidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * An {@link JobIntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread - for Build.VERSION_CODES.O and up.
 */
public class WidgetUpdateJobIntentService extends JobIntentService {
    public static final String LOG_TAG = WidgetUpdateJobIntentService.class.getName();
    public static final String ACTION_UPDATE_WEATHER_WIDGET_ONLINE =
            "com.example.android.exapleweatherwidget.action.update_weather_widget_online";
    // base URL for the query the weather api (APIXU)
    private static final String APIXU_REQUEST_URL = "https://api.apixu.com/v1/current.json?";
    public static final String RECEIVER = "receiver";
    private static final int JOB_ID = 2;

    // with this method we receive instructions to update the widget
    public static void enqueueWork(Context context, ServiceResultReceiver workerResultReceiver, String action) {
        Intent intent = new Intent(context, WidgetUpdateJobIntentService.class);
        intent.putExtra(RECEIVER, workerResultReceiver);
        intent.setAction(action);
        enqueueWork(context, WidgetUpdateJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(LOG_TAG, "onHandleWork activated");
        if (intent.getAction() != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WEATHER_WIDGET_ONLINE.equals(action)) {
                handleActionUpdateWeatherWidget();
            }
        }
    }

    /**
     * Handle action ACTION_UPDATE_WEATHER_WIDGET_ONLINE
     */
    public void handleActionUpdateWeatherWidget() {
        Weather weather = null;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds
                (new ComponentName(this, WeatherWidgetProvider.class));
        // get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        if (!isConnected) { // if there is no internet connection - don't show progress bar and set
            Log.i(LOG_TAG, "handleActionUpdateWeatherWidget activated - no Internet");
        }
        else {
            weather = WidgetQueryUtils.fetchWeather(createURI()); // fetch the weather data
        }
        WeatherWidgetProvider.updateWeatherWidgets(this, appWidgetManager, weather, appWidgetIds);
    }

    public String createURI() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String queryCity = sharedPrefs.getString(getString(R.string.settings_city_key),
                getString(R.string.settings_city_default));
        Uri baseUri = Uri.parse(APIXU_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(getString(R.string.query_by_city), queryCity);
        uriBuilder.appendQueryParameter("key", getResources().getString(R.string.api_key));
        Log.i(LOG_TAG, uriBuilder.toString());
        return uriBuilder.toString();
    }

    // Helper methods related to requesting and receiving current weather from APIXU
    private static class WidgetQueryUtils {
        private static final String LOG_TAG = WidgetUpdateJobIntentService.WidgetQueryUtils.class.getName();
        private static final int READ_TIMEOUT = 10000; /* milliseconds */
        private static final int CONNECT_TIMEOUT = 15000; /* milliseconds */

        private WidgetQueryUtils() {
        }

        // Query the APIXU and return a Weather object.
        private static Weather fetchWeather(String requestUrl) {
            // Create URL object
            URL url = queryCreateUrl(requestUrl);
            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = null;
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }
            return extractFeatureFromJson(jsonResponse);
        }

        // Returns new URL object from the given string URL.
        private static URL queryCreateUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error with creating URL ", e);
            }
            return url;
        }

        // Make an HTTP request to the given URL and return a String as the response.
        private static String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            // If the URL is null, then return early.
            if (url == null) {
                return jsonResponse;
            }
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(READ_TIMEOUT);
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // If the request was successful (response code 200),
                // then read the input stream and parse the response.
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) { //HTTP_OK = 200
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        // Convert the {@link InputStream} into a String which contains the
        // whole JSON response from the server.
        private static String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        // Return a Weather object that has been built up from
        // parsing the given JSON response.
        private static Weather extractFeatureFromJson(String weatherJSON) {
            // If the JSON string is empty or null, then return early.
            Weather currentWeather = null;
            if (TextUtils.isEmpty(weatherJSON)) {
                Log.e(LOG_TAG, "response is empty");
                return null;
            }
            try {
                // Create JSONObjects from the JSON response string
                JSONObject baseJsonObject = new JSONObject(weatherJSON);
                JSONObject location = baseJsonObject.getJSONObject("location");
                JSONObject current = baseJsonObject.getJSONObject("current");
                // extract city
                String city = location.optString("name").trim();
                String lastUpdated = current.optString("last_updated").trim();
                String tempC = current.getString("temp_c").trim();
                JSONObject currentCondition = current.getJSONObject("condition");
                String cCondition = currentCondition.optString("text").trim();
                int isDay = current.optInt("is_day");
                String windKph = current.optString("wind_kph").trim();
                String windDir = current.optString("wind_dir").trim();
                String currentHumidity = current.optString("humidity").trim();
                String feelsLikeC = current.optString("feelslike_c").trim();
                currentWeather = new Weather(city, isDay, lastUpdated, tempC, windKph, windDir, cCondition,
                        currentHumidity, feelsLikeC);


            } catch (JSONException e) {
                // If an error is thrown when executing any of the above statements in the "try" block,
                // catch the exception here, so the app doesn't crash. Print a log message
                // with the message from the exception.
                Log.e(LOG_TAG, "Problem parsing the weather JSON results", e);
            }
            return currentWeather;
        }
    }
}
