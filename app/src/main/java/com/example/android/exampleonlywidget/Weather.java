package com.example.android.exampleonlywidget;

import android.os.Parcel;
import android.os.Parcelable;

public class Weather implements Parcelable {
    /** name of city */
    private String mCity;
    /** is it day or not - day=1*/
    private int mIsDay;
    /** date and time of current weather  */
    private String mLastUpdated;
    /** current temp */
    private String mCurrentTempC = NO_CURRENT_TEMP;
    /** current wind in kmh */
    private String mWindKph;
    /** current wind direction */
    private String mWindDir;
    /** date of forecast */
    private String mDate;
    /** minimum temp */
    private String mMinTemp;
    /* maximum temp */
    private String mMaxTemp;
    /* weather conditions */
    private String mConditions;
    /* humidity */
    private String mHumidity;
    /* what it feels like in celsius */
    private String mFeelslikeC;
    /** Constant value that represents no current temp - i.e. second type of constructor  */
    private static final String NO_CURRENT_TEMP = null;

    /**
     * Create a new Weather object - first type.
     * @param city
     *
     * @param isDay
     *
     * @param lastUpdated
     *
     * @param tempC
     *
     * @param windKph
     *
     * @param windDir
     *
     * @param conditions
     *
     * @param humidity
     *
     * @param feelslikeC
     *
     */
    public Weather(String city, int isDay, String lastUpdated, String tempC, String windKph, String windDir,
                   String conditions, String humidity, String feelslikeC) {
        mCity = city;
        mIsDay = isDay;
        mLastUpdated = lastUpdated;
        mCurrentTempC = tempC;
        mWindKph = windKph;
        mWindDir = windDir;
        mConditions = conditions;
        mHumidity = humidity;
        mFeelslikeC = feelslikeC;
    }

    /**
     * Create a new Weather object - second type.
     * @param city
     *
     * @param date
     *
     * @param minTemp
     *
     * @param maxTemp
     *
     * @param conditions
     *
     * @param humidity
     */
    public Weather(String city, String date, String minTemp, String maxTemp, String conditions, String humidity) {
        mCity = city;
        mDate = date;
        mMinTemp = minTemp;
        mMaxTemp = maxTemp;
        mConditions = conditions;
        mHumidity = humidity;
    }

    // Get the city
    public String getCity() {
        return mCity;
    }

    // Get the last updated time and date
    public int getIsDay() {
        return mIsDay;
    }

    // Get the last updated time and date
    public String getLastUpdated() {
        return mLastUpdated;
    }

    // Get the current c
    public String getCurrentTemp() {
        return mCurrentTempC;
    }

    // Get the current wind
    public String getWindKph() { return mWindKph; }

    // Get the current wind direction
    public String getWindDir() { return mWindDir; }

    // Get the date of forecast
    public String getDate() {
        return mDate;
    }

    // Get the min temp
    public String getMinTemp() {
        return mMinTemp;
    }

    // Get the max temp
    public String getMaxTemp() { return mMaxTemp; }

    // Get the weather conditions
    public String getConditions() { return mConditions; }

    // Get the humidity
    public String getHumidity() {return mHumidity; }

    // Get the feel like temp
    public String getFeelsLikeC() {return mFeelslikeC; }

    /**
     * returns true if there is a currentTempC  - i.e. first type of constructor.
     */
    public boolean hasCurrentTEmp() { return mCurrentTempC != NO_CURRENT_TEMP; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mCity);
        dest.writeInt(this.mIsDay);
        dest.writeString(this.mLastUpdated);
        dest.writeString(this.mCurrentTempC);
        dest.writeString(this.mWindKph);
        dest.writeString(this.mWindDir);
        dest.writeString(this.mDate);
        dest.writeString(this.mMinTemp);
        dest.writeString(this.mMaxTemp);
        dest.writeString(this.mConditions);
        dest.writeString(this.mHumidity);
        dest.writeString(this.mFeelslikeC);
    }

    protected Weather(Parcel in) {
        this.mCity = in.readString();
        this.mIsDay = in.readInt();
        this.mLastUpdated = in.readString();
        this.mCurrentTempC = in.readString();
        this.mWindKph = in.readString();
        this.mWindDir = in.readString();
        this.mDate = in.readString();
        this.mMinTemp = in.readString();
        this.mMaxTemp = in.readString();
        this.mConditions = in.readString();
        this.mHumidity = in.readString();
        this.mFeelslikeC = in.readString();
    }

    public static final Parcelable.Creator<Weather> CREATOR = new Parcelable.Creator<Weather>() {
        @Override
        public Weather createFromParcel(Parcel source) {
            return new Weather(source);
        }

        @Override
        public Weather[] newArray(int size) {
            return new Weather[size];
        }
    };
}

