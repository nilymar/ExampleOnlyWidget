package com.example.android.exampleonlywidget;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;

// Helper methods related to requesting and receiving weather forecast from APIXU
public final class QueryUtils {
    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private static final int READ_TIMEOUT = 10000; /* milliseconds */
    private static final int CONNECT_TIMEOUT = 15000; /* milliseconds */

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    // Query the APIXU and return a list of {@link Weather} objects.
    public static List<Weather> fetchWeathers(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        // Extract relevant fields from the JSON response and create a list of {@link Weather}s
        List<Weather> weathers = extractFeatureFromJson(jsonResponse);
        // Return the list of {@link Weather}s
        return weathers;
    }

    // Returns new URL object from the given string URL.
    private static URL createUrl(String stringUrl) {
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

    // Return a list of {@link Weather} objects that has been built up from
    // parsing the given JSON response.
    private static List<Weather> extractFeatureFromJson(String weatherJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(weatherJSON)) {
            Log.e(LOG_TAG, "response is empty");
            return null;
        }
        // Create an empty ArrayList that we can start adding weathers to
        List<Weather> weathers = new ArrayList<>();
        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Create JSONObjects from the JSON response string
            JSONObject baseJsonObject = new JSONObject(weatherJSON);
            JSONObject location = baseJsonObject.getJSONObject("location");
            JSONObject current = baseJsonObject.getJSONObject("current");
            JSONObject forecast = baseJsonObject.getJSONObject("forecast");
            // extract city of the forecast
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
            weathers.add(new Weather(city, isDay, lastUpdated, tempC, windKph, windDir, cCondition,
                    currentHumidity, feelsLikeC));
            //extracting the data for current weather conditions, will be put in first cell in the weather array
            // Extract the JSONArray associated with the key called "forecastday",
            // which represents a list of forecasts (or weathers).
            JSONArray forecastArray = forecast.getJSONArray("forecastday");
            // For each forecast in the forecastArray, create an {@link Weather} object
            for (int i = 0; i < forecastArray.length(); i++) {
                // Get a single weather at position i within the list of weathers
                JSONObject currentForecast = forecastArray.getJSONObject(i);
                // extract title of the forecast - the date
                String date = currentForecast.optString("date").trim();
                JSONObject currentDay = currentForecast.getJSONObject("day");
                // extract min temp
                String minTemp = currentDay.optString("mintemp_c").trim();
                // extract max temp
                String maxTemp = currentDay.optString("maxtemp_c").trim();
                // Extract the conditions
                JSONObject conditionObject = currentDay.getJSONObject("condition");
                String condition = conditionObject.optString("text").trim();
                // Extract the humidity
                String humidity = currentDay.optString("avghumidity").trim();
                // Create a new {@link Weather} object with the title, date, author, section
                // and url from the JSON response.
                Weather weather = new Weather(city, date, minTemp, maxTemp, condition, humidity);
                // Add the new {@link Weather} to the list of weathers.
                weathers.add(weather);
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the weather JSON results", e);
        }
        return weathers;
    }
}