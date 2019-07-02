package com.example.android.exampleonlywidget;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {
    private ArrayList<Weather> weathers = new ArrayList<>();
    private Context mContext;

    // this is the constructor for the Weather object adapter
    public WeatherAdapter(Context context, ArrayList<Weather> mWeathers) {
        mContext = context;
        weathers = mWeathers;
    }

    public class WeatherViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.card_view)
        CardView cardView;
        @BindView(R.id.current_layout)
        RelativeLayout currentLayout;
        @BindView(R.id.forecast_layout)
        ConstraintLayout forecastLayout;
        @BindView(R.id.current_temp)
        TextView tempField;
        @BindView(R.id.current_wind)
        TextView windField;
        @BindView(R.id.current_wind_dir)
        TextView windDirField;
        @BindView(R.id.current_conditions)
        TextView conditionsField;
        @BindView(R.id.current_humidity)
        TextView humidityField;
        @BindView(R.id.current_title)
        TextView currentTitle;
        @BindView(R.id.current_time)
        TextView currentTime;
        @BindView(R.id.current_icon)
        ImageView currentIcon;
        @BindView(R.id.current_temp_text)
        TextView currentTemp;
        @BindView(R.id.current_wind_text)
        TextView currentWind;
        @BindView(R.id.current_wind_dir_text)
        TextView currentWindDir;
        @BindView(R.id.current_conditions_text)
        TextView currentConditions;
        @BindView(R.id.current_humidity_text)
        TextView currentHumidity;
        @BindView(R.id.date_of_forecast)
        TextView forecastDate;
        @BindView(R.id.min_temp_text)
        TextView minTemp;
        @BindView(R.id.max_temp_text)
        TextView maxTemp;
        @BindView(R.id.conditions_text)
        TextView conditions;
        @BindView(R.id.humidity_text)
        TextView humidity;
        @BindView(R.id.condition_icon)
        ImageView conditionIcon;

        public WeatherViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
        }

        public void bindWeathers(Weather currentWeather) {
            // i.e. second constructor
            if (currentWeather.hasCurrentTEmp()) {
                cardView.setRadius(0);
                cardView.setCardElevation(25);
                currentLayout.setVisibility(View.VISIBLE);
                forecastLayout.setVisibility(View.GONE);
                long timeLong = getCreatedAtTime(currentWeather.getLastUpdated());
                Date timeObject = new Date(timeLong);
                // the format of time on screen
                String formatTime = formatTime(timeObject);
                currentTime.setText(formatTime);
                Float curTemp = Float.parseFloat(currentWeather.getCurrentTemp());
                int currTemp = Math.round(curTemp); // to get a rounded temp number
                String currenTemp = currTemp + " \u2103"; // adding celsius symbol to the temp
                currentTemp.setText(currenTemp);
                String curWind = currentWeather.getWindKph();
                String currWind = curWind + " Kph";
                currentWind.setText(currWind);
                String curWindDir = currentWeather.getWindDir();
                currentWindDir.setText(curWindDir);
                String condition = currentWeather.getConditions();
                currentConditions.setText(condition);
                // replacing the ".0" with percentage symbol in humidity
                String humidityText = currentWeather.getHumidity().replace(".0", "");
                humidityText += "%";
                currentHumidity.setText(humidityText);
                // getting the possible weather conditions and matching icon list from arrays.xml file
                if (currentWeather.getIsDay() == 1) {
                    String[] conditions = mContext.getResources().getStringArray(R.array.day_conditions);
                    String[] icons = mContext.getResources().getStringArray(R.array.day_icons);
                    // put the first icon in the list, in case no icon is found
                    currentIcon.setImageResource(mContext.getResources().
                            getIdentifier(icons[0], null, mContext.getPackageName()));
                    // put the right background for day weather
                    currentLayout.setBackgroundResource(R.color.light_blue);
                    // search for the right icon for the weather condition
                    for (int i = 0; i < conditions.length; i++) {
                        // setting the right condition icon to the weather condition
                        if (condition.equalsIgnoreCase(conditions[i])) {
                            currentIcon.setImageResource(mContext.getResources().
                                    getIdentifier(icons[i], null, mContext.getPackageName()));
                            break; // if the icon was found - don't continue with the for loop
                        }
                    }
                } else {
                    String[] conditions = mContext.getResources().getStringArray(R.array.night_conditions);
                    String[] icons = mContext.getResources().getStringArray(R.array.night_icons);
                    // change the text color to night color
                    tempField.setTextColor(mContext.getResources().getColor(R.color.night_text));
                    windField.setTextColor(mContext.getResources().getColor(R.color.night_text));
                    windDirField.setTextColor(mContext.getResources().getColor(R.color.night_text));
                    conditionsField.setTextColor(mContext.getResources().getColor(R.color.night_text));
                    humidityField.setTextColor(mContext.getResources().getColor(R.color.night_text));
                    currentTitle.setTextColor(mContext.getResources().getColor(R.color.night_title));
                    currentTime.setTextColor(mContext.getResources().getColor(R.color.night_title));
                    currentTemp.setTextColor(mContext.getResources().getColor(R.color.night_text));
                    currentWind.setTextColor(mContext.getResources().getColor(R.color.night_text));
                    currentWindDir.setTextColor(mContext.getResources().getColor(R.color.night_text));
                    currentConditions.setTextColor(mContext.getResources().getColor(R.color.night_text));
                    currentHumidity.setTextColor(mContext.getResources().getColor(R.color.night_text));
                    currentIcon.setImageResource(mContext.getResources().
                            getIdentifier(icons[0], null, mContext.getPackageName()));
                    // change the background color to night background
                    currentLayout.setBackgroundResource(R.color.night_dark);
                    // search for the right icon for the weather condition
                    for (int i = 0; i < conditions.length; i++) {
                        // setting the right condition icon to the weather condition
                        if (condition.equalsIgnoreCase(conditions[i])) {
                            currentIcon.setImageResource(mContext.getResources().
                                    getIdentifier(icons[i], null, mContext.getPackageName()));
                            break; // if the icon was found - don't continue with the for loop
                        }
                    }
                }
            } else {// for the forecast items
                currentLayout.setVisibility(View.GONE);
                forecastLayout.setVisibility(View.VISIBLE);
                long dateLong = getCreatedAt(currentWeather.getDate());
                Date dateObject = new Date(dateLong);
                // the format of your date and time on screen
                String formattedDate = formatDate(dateObject);
                forecastDate.setText(formattedDate);
                Float minT = Float.parseFloat(currentWeather.getMinTemp());
                int minTint = Math.round(minT); // to get a rounded temp number
                String tempMin = minTint + " \u2103"; // adding celsius symbol to the temp
                minTemp.setText(tempMin);
                Float maxT = Float.parseFloat(currentWeather.getMaxTemp());
                int maxTint = Math.round(maxT);// to get a rounded temp number
                String tempMax = maxTint + " \u2103"; // adding celsius symbol to the temp
                maxTemp.setText(tempMax);
                String condition = currentWeather.getConditions();
                conditions.setText(condition);
                // replacing the ".0" with percentage symbol in humidity
                String humidityText = currentWeather.getHumidity().replace(".0", "%");
                humidity.setText(humidityText);
                // getting the possible weather conditions and matching icon list from arrays.xml file
                String[] conditions = mContext.getResources().getStringArray(R.array.day_conditions);
                String[] icons = mContext.getResources().getStringArray(R.array.day_icons);
                conditionIcon.setImageResource(mContext.getResources().
                        getIdentifier(icons[0], null, mContext.getPackageName()));
                for (int i = 0; i < conditions.length; i++) {
                    // setting the right condition icon to the weather condition
                    if (condition.equalsIgnoreCase(conditions[i])) {
                        conditionIcon.setImageResource(mContext.getResources().
                                getIdentifier(icons[i], null, mContext.getPackageName()));
                        break; // if the icon was found - don't continue with the for loop
                    }
                }

            }
        }

        @Override
        public void onClick(View v) {
            // didn't put anything here
        }
    }

    // Return the formatted date string (i.e. "Jan 14, 1978") from a Date object.
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE (LLL dd, yyyy)");
        return dateFormat.format(dateObject);
    }

    // Return the formatted time string (i.e. "4:30 PM") from a Date object - left it here so you can use it, no need in this app.
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(dateObject);
    }

    // create a date format from the string we get from the json stream
    public long getCreatedAt(String createdAt) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date createDate = null;
        try {
            createDate = formatter.parse(createdAt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i("weatherAdapter","getCreatedAt result is " + createDate.getTime());
        return createDate.getTime();
    }

    public long getCreatedAtTime(String createdAt) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date createDate = null;
        try {
            createDate = formatter.parse(createdAt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i("weatherAdapter","getCreatedAt result is " + createDate.getTime());
        return createDate.getTime();
    }

    @Override
    public WeatherAdapter.WeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        WeatherViewHolder viewHolder = new WeatherViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WeatherAdapter.WeatherViewHolder holder, final int position) {
        holder.bindWeathers(weathers.get(position)); // binding the view to the right object
    }

    @Override
    public int getItemCount() {
        return weathers.size();
    }

}
