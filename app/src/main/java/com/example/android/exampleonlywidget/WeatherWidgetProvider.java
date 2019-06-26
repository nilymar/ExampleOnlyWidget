package com.example.android.exampleonlywidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidgetProvider extends AppWidgetProvider {
    public static final String LOG_TAG = WeatherWidgetProvider.class.getName();
    public static final String ACTION_UPDATE_WEATHER_WIDGET_ONLINE =
            "com.example.android.exapleweatherwidget.action.update_weather_widget_online";

    // updating an app widget, using different views for narrow and wide widgets
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                Weather weather, int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views;
        // Create an Intent to launch MainActivity when clicked
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        if (width < 200) {
            views = getSmallRemoteView(context, weather); // for a narrow widget
        } else {
            views = getBigRemoteView(context, weather); // for a wide widget
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Creates and returns the RemoteViews to be displayed in the small mode widget
     * @param context   The context
     * @param weather   The current weather object with all its data
     * @return The RemoteViews for the small display mode widget
     */
    private static RemoteViews getSmallRemoteView(Context context, Weather weather){
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        // Set the click handler to open the MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        String time = "-";
        String widgetTemp = "-";
        int weatherIcon = R.drawable.a113;
        String city = "-";
        boolean isDay = true;
        if (weather == null) {
            widgetTemp = "No data";
        }
        else {
            city = weather.getCity();
            long timeLong = getCreatedAtTime(weather.getLastUpdated());
            Date timeObject = new Date(timeLong);
            // the format of time on screen
            time = formatTime(timeObject);
            String temp = weather.getCurrentTemp();
            Float curTemp = Float.parseFloat(temp);
            int currTemp = Math.round(curTemp); // to get a rounded temp number
            widgetTemp = String.valueOf(currTemp) + " \u2103";
            String condition = weather.getConditions();
            if (weather.getIsDay() == 1) {
                String[] conditions = context.getResources().getStringArray(R.array.day_conditions);
                String[] icons = context.getResources().getStringArray(R.array.day_icons);
                isDay = true;
                for (int i = 0; i < conditions.length; i++) {
                    // setting the right condition icon to the weather condition
                    if (condition.equalsIgnoreCase(conditions[i])) {
                        weatherIcon = context.getResources().
                                getIdentifier(icons[i], null, context.getPackageName());
                        Log.i(LOG_TAG, "icon location is " + weatherIcon);
                        break; // if the icon was found - don't continue with the for loop
                    }
                }
            } else {
                isDay = false;
                String[] conditions = context.getResources().getStringArray(R.array.night_conditions);
                String[] icons = context.getResources().getStringArray(R.array.night_icons);
                for (int i = 0; i < conditions.length; i++) {
                    // setting the right condition icon to the weather condition
                    if (condition.equalsIgnoreCase(conditions[i])) {
                        weatherIcon = context.getResources().
                                getIdentifier(icons[i], null, context.getPackageName());
                        Log.i(LOG_TAG, "icon location is " + weatherIcon);
                        break; // if the icon was found - don't continue with the for loop
                    }
                }
            }
        }
        // setting the strings in the right places in the views
        views.setTextViewText(R.id.current_city, city);
        views.setTextViewText(R.id.current_time, time);
        views.setTextViewText(R.id.current_temp, widgetTemp);
        views.setImageViewResource(R.id.widget_image, weatherIcon);
        if (isDay) { // if it is day - the text is black and background light blue
            views.setTextColor(R.id.current_city, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.current_time, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.current_temp, context.getResources().getColor(R.color.text_color));
            views.setImageViewResource(R.id.widget_background, R.color.light_blue);
        } else { // if it is night - the text is white and background night dark
            views.setTextColor(R.id.current_city, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.current_temp, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.current_time, context.getResources().getColor(R.color.night_text));
            views.setImageViewResource(R.id.widget_background, R.color.night_dark);
        }
        // clicking anywhere in the widget - will activate the pending intent
        views.setOnClickPendingIntent(R.id.widget_background, pendingIntent);
        return views;
    }

    /**
     * Creates and returns the RemoteViews to be displayed in the big mode widget
     * @param context   The context
     * @param weather   The weater object with all its data
     * @return The RemoteViews for the small display mode widget
     */
    private static RemoteViews getBigRemoteView(Context context, Weather weather){
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_wide_view);
        // Set the click handler to open the MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        String time = "-";
        String widgetTemp = "-";
        String feelLike = "-";
        int weatherIcon = R.drawable.a113;
        String city = "-";
        String humidity = "-";
        String wind = "-";
        String windDir = "-";
        boolean isDay = true;
        if (weather == null) {
            widgetTemp = "No data";
        }
        else {
            city = weather.getCity();
            humidity = weather.getHumidity();
            humidity += "%";
            // creating the feel like temp display
            feelLike = weather.getFeelsLikeC();
            Float feelL = Float.parseFloat(feelLike);
            int feelLi = Math.round(feelL); // to get a rounded temp number
            feelLike = feelLi + " \u2103";
            // adding kph to the wind string
            wind = weather.getWindKph() + " kph";
            windDir= weather.getWindDir();
            long timeLong = getCreatedAtTime(weather.getLastUpdated());
            Date timeObject = new Date(timeLong);
            // the format of time on screen
            time = formatTime(timeObject);
            widgetTemp = weather.getCurrentTemp();
            Float curTemp = Float.parseFloat(widgetTemp);
            int currTemp = Math.round(curTemp); // to get a rounded temp number
            widgetTemp = currTemp + " \u2103"; // adding celsius sign to the number
            String condition = weather.getConditions();
            if (weather.getIsDay() == 1) {
                String[] conditions = context.getResources().getStringArray(R.array.day_conditions);
                String[] icons = context.getResources().getStringArray(R.array.day_icons);
                isDay = true;
                for (int i = 0; i < conditions.length; i++) {
                    // setting the right condition icon to the weather condition
                    if (condition.equalsIgnoreCase(conditions[i])) {
                        weatherIcon = context.getResources().
                                getIdentifier(icons[i], null, context.getPackageName());
                        break; // if the icon was found - don't continue with the for loop
                    }
                }
            } else {
                isDay = false;
                String[] conditions = context.getResources().getStringArray(R.array.night_conditions);
                String[] icons = context.getResources().getStringArray(R.array.night_icons);
                for (int i = 0; i < conditions.length; i++) {
                    // setting the right condition icon to the weather condition
                    if (condition.equalsIgnoreCase(conditions[i])) {
                        weatherIcon = context.getResources().
                                getIdentifier(icons[i], null, context.getPackageName());
                        break; // if the icon was found - don't continue with the for loop
                    }
                }
            }
        }
        views.setTextViewText(R.id.current_city, city);
        views.setTextViewText(R.id.current_time, time);
        views.setTextViewText(R.id.current_temp, widgetTemp);
        views.setTextViewText(R.id.feel_like, feelLike);
        views.setTextViewText(R.id.humidity, humidity);
        views.setTextViewText(R.id.wind, wind);
        views.setTextViewText(R.id.wind_dir, windDir);
        views.setImageViewResource(R.id.widget_image, weatherIcon);
        if (isDay) { // setting the right colors for day display
            views.setTextColor(R.id.current_city, context.getResources().getColor(R.color.colorPrimary));
            views.setTextColor(R.id.current_time, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.current_temp, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.feel_like, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.humidity, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.wind, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.wind_dir, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.current_temp_title, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.current_time_title, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.feel_like_title, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.humidity_title, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.wind_title, context.getResources().getColor(R.color.text_color));
            views.setTextColor(R.id.wind_dir_title, context.getResources().getColor(R.color.text_color));
            views.setImageViewResource(R.id.widget_background, R.color.light_blue);
        } else { // setting the right colors for night display
            views.setTextColor(R.id.current_city, context.getResources().getColor(R.color.background_main));
            views.setTextColor(R.id.current_temp, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.current_time, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.feel_like, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.humidity, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.wind, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.wind_dir, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.current_temp_title, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.current_time_title, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.feel_like_title, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.humidity_title, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.wind_title, context.getResources().getColor(R.color.night_text));
            views.setTextColor(R.id.wind_dir_title, context.getResources().getColor(R.color.night_text));
            views.setImageViewResource(R.id.widget_background, R.color.night_dark);
        }
        // clicking anywhere in the widget - will activate the pending intent
        views.setOnClickPendingIntent(R.id.widget_background, pendingIntent);
        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ServiceResultReceiver mServiceResultReceiver = new ServiceResultReceiver(new Handler());
        WidgetUpdateJobIntentService.enqueueWork(context, mServiceResultReceiver,
                ACTION_UPDATE_WEATHER_WIDGET_ONLINE);
    }

    // method for updating all instances of the widget
    public static void updateWeatherWidgets(Context context, AppWidgetManager appWidgetManager, Weather weather,
                                            int[] appWidgetIds){
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, weather, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        RemoteViews defaultViews = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        ComponentName comp = new ComponentName(context.getPackageName(), WeatherWidgetProvider.class.getName());
        int[] appWidgetIds = mgr.getAppWidgetIds(comp);
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            Intent idefault = new Intent(context, MainActivity.class);
            idefault.putExtra("widget", "1");
            PendingIntent defaultpendingIntent = PendingIntent.getActivity(context, 0, idefault, 0);
            defaultViews.setOnClickPendingIntent(R.id.widget_background, defaultpendingIntent);
            comp = new ComponentName(context.getPackageName(), WeatherWidgetProvider.class.getName());
            mgr.updateAppWidget(comp, defaultViews);
        }
    }

    @Override
    public void onDisabled(Context context) {
        Log.i(LOG_TAG, "onDisabled activated");
    }

    // handle updating widget when there are changes in size
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                          Bundle newOptions) {
        ServiceResultReceiver mServiceResultReceiver = new ServiceResultReceiver(new Handler());
        WidgetUpdateJobIntentService.enqueueWork(context, mServiceResultReceiver,
                ACTION_UPDATE_WEATHER_WIDGET_ONLINE);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }


    // Return the formatted time string (i.e. "4:30 PM") from a Date object
    private static String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(dateObject);
    }

    public static long getCreatedAtTime(String createdAt) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date createDate = null;
        try {
            createDate = formatter.parse(createdAt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return createDate.getTime();
    }
}

