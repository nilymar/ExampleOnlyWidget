# ExampleOnlyWidget
a widget only app, based on the weather app in here: https://github.com/nilymar/ExampleWeatherAPI

 ----------------------------------------------------------------------------------------------------------------------------------------- 
 !!! ATTENTION!!!!! - the api was replaced by a new one - weatherstack - I didn't update the code here - so it will require some work....
 ----------------------------------------------------------------------------------------------------------------------------------------- 

this app is a widget only app - i.e. the app won't appear in the apps drawer after installment, only in the widgets drawer.

***YOU WILL NEED TO GET AN API KEY FROM APIXU TO USE THIS APP!!!! put it in api_key in the strings.xml file ***

to create a widget only app you need to go the Run -> Edit configurations - > Launcheroptions -> Launch: choose Nothing. also, in the 
manifest make sure no activity is a launcher activity.
*************************************************************************************************************************************

This app uses APIXU: https://www.apixu.com/ to fetch current weather and forecast, for a specify city and time period. when you
install the widget on screen, the setting screen will open, so you can change the city and forecast period. The widget has two posible
layout, depending on its width. It is updated every half an hour using JobIntentService (so that it will work on the newer versions of
android.

*************************************************************************************************************************************
To make sure the setting screen will pop up on first on screen installation you need to add an intent-filter to the setting activity in
the manifest file, with the action: "android.appwidget.action.APPWIDGET_CONFIGURE"
In the xml file for the widget provider info you will need to add "android:configure" and the full path for the setting activity name.
Then in the setting activity "onCreate" you add an intent to get the widget id  and in the same activity - you will need to update the
widget on change in setting, and use an intent to send out the widght id. you can look at the changes made in the settingActivity file
compared to the one made in ExampleWeather API
**************************************************************************************************************************************

![5d133626b1678895942516](https://user-images.githubusercontent.com/33417968/60167229-559e2300-980b-11e9-8545-2592531336b5.gif)

