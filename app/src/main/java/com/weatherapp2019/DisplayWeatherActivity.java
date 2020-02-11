package com.weatherapp2019;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.weatherapp2019.JSONClasses.*;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherapp2019.ThreeHourForecast.CompleteForecast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class DisplayWeatherActivity extends AppCompatActivity {
    public static final int citySaved = 123;
    public static final int failedSearch = 124;
    String cityName;
    String intentSource;
    Context context = this;
    LinearLayout ll;
    SavedCitiesHelper db;
    Complete complete;
    View displayWeatherLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Basic setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_weather);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Receive the user's search from Intent
        Intent weatherIntent = getIntent();
        cityName = weatherIntent.getStringExtra(MainActivity.cityNameCode);
        intentSource = weatherIntent.getStringExtra(MainActivity.intentSourceCode);
        ConstraintLayout root2 = findViewById(R.id.root2);

        // Set up linearLayout
        ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        root2.addView(ll);

        // Instantiate database
        db = new SavedCitiesHelper(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        displayWeatherLayout = inflater.inflate(R.layout.local_weather_template, null, false);

        // Get detailed weather and initialize save button
        GetDetailedWeather getWeather = new GetDetailedWeather();
        getWeather.execute("https://api.openweathermap.org/data/2.5/weather?q=" + cityName + ",us&appid=9035a183aea3ebf0f7fe2c28dc04c7b3",
                "https://api.openweathermap.org/data/2.5/forecast?q=" + cityName + "&appid=9035a183aea3ebf0f7fe2c28dc04c7b3");
    }

    // Call getComplete. Pass Complete object to buildDisplayWeather. Then add saveCity button to activity.
    private class GetDetailedWeather extends AsyncTask<String, Void, CompletePlusForecast> {

        // Call getComplete. Pass Complete object to onPostExecute
        protected CompletePlusForecast doInBackground(String... strings) {
            try{
                Complete complete = MainActivity.getComplete(strings[0]);
                CompleteForecast forecast = MainActivity.getCompleteForecast(strings[1]);
                CompletePlusForecast completePlusForecast = new CompletePlusForecast(complete, forecast);
                return completePlusForecast;
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        // Pass Complete object to buildDisplayWeather and make save Button
        protected void onPostExecute(CompletePlusForecast result) {
            try {
                System.err.println(result.complete.name);
                buildDisplayWeather(result.complete, result.forecast);

                complete = result.complete;

                // If city was searched, Make save button and set it's click to saveCity(cityID)
                if(intentSource.equals("searchedCity")) {
                    Button saveCity = new Button(context);
                    saveCity.setText("Add To Favorites");
                    final long cityID = result.complete.id;
                    saveCity.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            saveCity(cityID);
                        }
                    });
                    ll.addView(saveCity);
                }


            }catch(Exception e) {
                //City was not able to be searched
                // startActivityForResult(intent, failedSearch );
                e.printStackTrace();
                setResult(failedSearch);
                finish();

            }
        }
    }

    // Inflate layout and populate using Complete parameter. Then add layout to ll
    private void buildDisplayWeather(Complete complete, CompleteForecast forecast) {

        // Lookup views from display_weather_template.xml
        TextView cityName = (TextView) displayWeatherLayout.findViewById(R.id.localCityName);
        TextView temperature = (TextView) displayWeatherLayout.findViewById(R.id.localTemperature);
        TextView temperatureHelper = (TextView) displayWeatherLayout.findViewById(R.id.localTemperatureHelper);
        TextView temperatureHelper2 = (TextView) displayWeatherLayout.findViewById(R.id.localTemperatureHelper2);
        TextView minValue = (TextView) displayWeatherLayout.findViewById(R.id.minValue);
        TextView maxValue = (TextView) displayWeatherLayout.findViewById(R.id.maxValue);
        TextView humidityValue = (TextView) displayWeatherLayout.findViewById(R.id.humidityValue);
        TextView feelsLikeValue = (TextView) displayWeatherLayout.findViewById(R.id.feelsLikeValue);
        TextView description = (TextView) displayWeatherLayout.findViewById(R.id.localDescription);
        ImageView icon = (ImageView) displayWeatherLayout.findViewById(R.id.localWeather_icon);
        LinearLayout forecastList = (LinearLayout) displayWeatherLayout.findViewById(R.id.forecast_list);

        // Set text for views
        cityName.setText(complete.name);
        temperature.setText(String.valueOf(Math.round((complete.main.temp - 273.15) * (9 / 5.0) + 32)));
        temperatureHelper.setText("o");
        temperatureHelper2.setText("F");
        minValue.setText("Minimum:  " + String.valueOf(Math.round((complete.main.temp_min - 273.15) * (9 / 5.0) + 32)));
        maxValue.setText("Maximum: " + String.valueOf(Math.round((complete.main.temp_max - 273.15) * (9 / 5.0) + 32)));
        humidityValue.setText("Humidity:  " + String.valueOf(Math.round(complete.main.humidity)));
        feelsLikeValue.setText("Feels like:  " + String.valueOf(Math.round((complete.main.feels_like- 273.15) * (9 / 5.0) + 32)));
        description.setText(complete.weather[0].description);
        //set icon using switch function from customRecyclerAdapter class
        CustomRecyclerAdapter.setIcon(complete.weather[0].icon, icon);
        for(int i = 0; i < 9; i++) {
            LinearLayout forecastListItem = new LinearLayout(this);
            forecastListItem.setOrientation(LinearLayout.VERTICAL);

            TextView forecastTime = new TextView(this);
            long time = (((forecast.list[i].dt + forecast.city.timezone) % 86400) / 3600);
            String amOrPm;
            if(time >= 12) {
                amOrPm = ":00 PM";
            }else {
                amOrPm = ":00 AM";
            }
            time = time % 12;
            if(time == 0) {
                time = 12;
            }
            //forecastTime.setText(String.valueOf( (((forecast.list[i].dt + forecast.city.coord.timezone) % 86400) / 3600) %12 ));
            forecastTime.setText(time + amOrPm );
            forecastTime.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView forecastTemp = new TextView(this);
            forecastTemp.setText(String.valueOf(Math.round((forecast.list[i].main.temp- 273.15) * (9 / 5.0) + 32)) + "\u00B0");
            forecastTemp.setGravity(Gravity.CENTER_HORIZONTAL);

            ImageView weatherIcon = new ImageView(this);
            CustomRecyclerAdapter.setIcon(forecast.list[i].weather[0].icon, weatherIcon);

            forecastListItem.addView(forecastTime);
            forecastListItem.addView(forecastTemp);
            forecastListItem.addView(weatherIcon);
            System.out.println(forecastListItem);
            System.out.println(forecastList);
            forecastList.addView(forecastListItem);
            System.out.println("GGGGGGG");
            System.out.println("DDDDDDD");
        }

        // Add the layout to ll
        ll.addView(displayWeatherLayout);
    }

    //Save cityID in db and start MainActivity for onActivityResult
    public void saveCity(long cityID) {
        try {
            // Add data to database
            db.insertData(cityID);
            MainActivity.adapter.notifyItemInserted(MainActivity.adapter.getItemCount());
            MainActivity.result2.add(complete);
            db.close();

            // Send intent that will call onActivityResult() in MainActivity
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivityForResult(intent, saveCityCode );
            setResult(citySaved);
            finish();

        } catch (Error e) {
            // Snackbar failure message
            Snackbar snackbarFailure = Snackbar.make(findViewById(R.id.root2), R.string.snackbar_failure_message, BaseTransientBottomBar.LENGTH_SHORT);
            snackbarFailure.show();
        }
    }
}