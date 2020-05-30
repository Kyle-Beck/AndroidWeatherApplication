package com.weatherapp2019;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.weatherapp2019.JSONClasses.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.weatherapp2019.ThreeHourForecast.CompleteForecast;
import static com.weatherapp2019.DisplayWeatherActivity.citySaved;
import static com.weatherapp2019.DisplayWeatherActivity.failedSearch;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    static final String cityNameCode = "cityNameCode";
    static final String intentSourceCode = "intentSourceCode";
    static final int passCityCode = 200;
    static final int locationPERMISSION_ID = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.findViewById(R.id.button).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                EditText cityName = findViewById(R.id.editText);
                passCity(cityName.getText().toString(), "searchedCity");
            }
        });

        initiateLocalWeather(this);
        GetSavedWeather getSavedWeather = new GetSavedWeather(this);

        getSavedWeather.execute("hi");
    }

    //todo: PROBLEM: INFINITE LOCATION REQUEST LOOP
    @Override
    protected void onRestart() {
        super.onRestart();
        if(isLocationEnabled() && checkPermissions()) {
            getLocation(this);
        } else{
            Snackbar.make(findViewById(R.id.root), "Please enable location in order to view current, local weather", BaseTransientBottomBar.LENGTH_LONG).show();
            //TODO: DISPLAY GENERIC CITY
        }

    }

    // Inflate OptionsMenu using menu_main res file.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handle menu item clicks here using item.id (Currently unused)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

///////////////////////// OpenWeatherMap Funcs /////////////////////////////////////////////////////

    public static CompleteForecast getCompleteForecast(String url) throws IOException{
            URL weatherMap = new URL(url);
            //creates a connection object, but does not establish the connection yet.
            URLConnection connection = weatherMap.openConnection();
            //connection.getInputStream() Establishes connection
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            //read inputStream from Connection
            String inputLine = in.readLine();
            //Create JSON object mapper
            ObjectMapper objectMapper = new ObjectMapper();
            //Use object mapper to initialize new Complete object according to JSON String and return it
            return objectMapper.readValue(inputLine, CompleteForecast.class);
    }

    public static Complete getComplete(String url) throws IOException{
        //initialize a URL object
        URL weatherMap = new URL(url);
        //creates a connection object, but does not establish the connection yet.
        URLConnection connection = weatherMap.openConnection();
        //establishes connection
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        //read inputStream from Connection
        String inputLine = in.readLine();
        //Create JSON object mapper
        ObjectMapper objectMapper = new ObjectMapper();
        //Use object mapper to parse JSON into objects
        return objectMapper.readValue(inputLine, Complete.class);
    }

    public static String getWeatherData(String url) throws IOException{
        URL weathermap = new URL(url);
        URLConnection conn = weathermap.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        return br.readLine();
    }

    public static Complete parseWeatherData(String data) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(data, Complete.class);
    }

///////////////////////////////////// UI Funcs  //////////////////////////////////////////////////

    // Call getComplete. Pass Complete object to buildLocalWeather
    private class GetLocalWeather extends AsyncTask<String, Void, CompletePlusForecast> {
        private WeakReference<MainActivity> activityRef;

        public GetLocalWeather(MainActivity context){
            this.activityRef = new WeakReference<MainActivity>(context);
        }

        protected CompletePlusForecast doInBackground(String... strings) {
            try{
                Complete complete = getComplete(strings[0]);
                CompleteForecast forecast = getCompleteForecast(strings[1]);
                CompletePlusForecast completePlusForecast = new CompletePlusForecast(complete, forecast);
                System.out.println("XXXXXXXXXXX");


                return completePlusForecast;
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }

        }
        protected void onPostExecute(CompletePlusForecast result) {
            MainActivity activity = this.activityRef.get();
            if (activity == null || activity.isFinishing()) return;
            try {
                buildLocalWeather(result.complete, result.forecast, activity);

            } catch(Exception e) {
                // if localCity was never gotten:

                TextView cityName = activity.findViewById(R.id.localCityName);
                cityName.setText("There was a problem getting local weather data at this time");

                Snackbar.make(findViewById(R.id.root), "Unable to get current, local weather data", BaseTransientBottomBar.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    }

    //TODO: USE WEAK REFERENCE TO POPULATE VIEWS
    // Populate localWeatherLayout using Complete parameter.
    private void buildLocalWeather(Complete complete, CompleteForecast forecast, MainActivity activity) {
        View localLayout = activity.findViewById(R.id.localWeather);

        localLayout.setOnClickListener(new PassLocalCityClickListener(activity, complete));

//        localLayout.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    passCity(localCity, "searchedCity");
//            }
//        });
        // Lookup view from template
        TextView cityName = (TextView) localLayout.findViewById(R.id.localCityName);
        TextView temperature = (TextView) localLayout.findViewById(R.id.localTemperature);
        TextView temperatureHelper = (TextView) localLayout.findViewById(R.id.localTemperatureHelper);
        TextView temperatureHelper2 = (TextView) localLayout.findViewById(R.id.localTemperatureHelper2);
        TextView minValue = (TextView) localLayout.findViewById(R.id.minValue);
        TextView maxValue = (TextView) localLayout.findViewById(R.id.maxValue);
        TextView humidityValue = (TextView) localLayout.findViewById(R.id.humidityValue);
        TextView feelsLikeValue = (TextView) localLayout.findViewById(R.id.feelsLikeValue);
        TextView description = (TextView) localLayout.findViewById(R.id.localDescription);
        ImageView icon = (ImageView) localLayout.findViewById(R.id.localWeather_icon);
        LinearLayout forecastList = (LinearLayout) localLayout.findViewById(R.id.forecast_list);

        // Set template text
        cityName.setText(complete.name);
        temperature.setText(String.valueOf(Math.round((complete.main.temp - 273.15) * (9 / 5.0) + 32)));
        temperatureHelper.setText("o");
        temperatureHelper2.setText("F");
        minValue.setText("Minimum:  " + String.valueOf(Math.round((complete.main.temp_min - 273.15) * (9 / 5.0) + 32)));
        maxValue.setText("Maximum: " + String.valueOf(Math.round((complete.main.temp_max - 273.15) * (9 / 5.0) + 32)));
        humidityValue.setText("Humidity:  " + String.valueOf(Math.round(complete.main.humidity)));
        feelsLikeValue.setText("Feels like:  " + String.valueOf(Math.round((complete.main.feels_like- 273.15) * (9 / 5.0) + 32)));
        description.setText(complete.weather[0].description);
        // Sets icon using static switch function from customArray class
        CustomRecyclerAdapter.setIcon(complete.weather[0].icon, icon);

        forecastList.removeAllViews();
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
            forecastList.addView(forecastListItem);
            System.out.println("JJJJJJJJJJJJJJJJJ");
            System.out.println("PPPPPPPPPPPPP");
        }
    }

    // Call getComplete for each city in database. Bind Complete array to RecyclerView using CustomRecyclerAdapter.
    @SuppressLint("StaticFieldLeak")
    private class GetSavedWeather extends AsyncTask<String, Void,  ArrayList<Complete>> {

        private WeakReference<MainActivity> activityRef;

        GetSavedWeather(MainActivity context){
            this.activityRef = new WeakReference<MainActivity>(context);
        }

        // Curse through database making calls to getComplete. Pass Complete list to onPostExecute
        protected  ArrayList<Complete> doInBackground(String... strings) {
            try{
                MainActivity activity = activityRef.get();
//                if (activity == null || activity.isFinishing()) return;
                // Use helper to instantiate database
                SavedCitiesHelper helper = new SavedCitiesHelper(activity);
                SQLiteDatabase db = helper.getReadableDatabase();

                // Create cursor on database
                Cursor cursor = db.query(SavedCitiesHelper.TABLE_NAME, null, null, null, null, null, null);

                // Initialize array that will hold complete objects
                ArrayList<Complete> list = new ArrayList<>();

                // Use cursor to call getComplete for each savedCity. Add complete objects to list
                while (cursor.moveToNext()) {
                    //Get cityID from database and use it to getComplete.
                    long cityID = cursor.getLong(cursor.getColumnIndexOrThrow(SavedCitiesHelper.COLUMN_2));
                    // Use cityID to getComplete object and add it to list
                    list.add(getComplete("https://api.openweathermap.org/data/2.5/weather?id=" + cityID + "&appid=9995cb7d60eb608728115ffa6fbe9580"));

                }
                cursor.close();

                //Pass list to onPostExecute
                return list;
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        // Bind list of Complete objects to recycler view using adapter. (Also swipe to delete)
        protected void onPostExecute(final ArrayList<Complete> result) throws NullPointerException {
            MainActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return;
            LinearLayout ll = activity.findViewById(R.id.linearLayout);

            // if result is null, throw nullPointerException. (Because we cant throw exceptions in doInBackground)
            if(result == null) {
                ll.addView(createMessage("Unable to get saved weather data", activity));
            } else {
                if(result.size() == 0) {
                    TextView noSavedCities = activity.findViewById(R.id.noSavedCities);
                    noSavedCities.setText("Add cities to your favorites by searching and clicking the 'Add To Favorites' button");
                }
                //Create adapter with result and add it to rv
                CustomRecyclerAdapter adapter = new CustomRecyclerAdapter(result, new ClickListener() {
                    @Override
                    public void onItemClick(Complete complete) {
                        passCity(complete.name, "savedCity");
                    }
                }, activity);

                RecyclerView rv = activity.findViewById(R.id.recyclerView);
                rv.setAdapter(adapter);
                rv.setLayoutManager(new GridLayoutManager(activity, 1));

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new MyCallback(activity, result, adapter));

                itemTouchHelper.attachToRecyclerView(rv);

            }
        }
    }

    // Returns a textView with given message
    public TextView createMessage(String s, MainActivity context) {
        TextView savedWeatherFailure = new TextView(context);
        savedWeatherFailure.setText(s);
        savedWeatherFailure.setGravity(Gravity.CENTER_HORIZONTAL);
        savedWeatherFailure.setTextSize(20);
        savedWeatherFailure.setPadding(10,30,10,30);
        return savedWeatherFailure;
    }

//////////////////////////////////// DisplayWeather Interaction Funcs //////////////////////////////

    // Used by Search button to Open DisplayWeatherActivity
    public void passCity(String cityName, String intentSource) {
        Intent intent = new Intent(this, DisplayWeatherActivity.class);
        intent.putExtra(cityNameCode, cityName);
        intent.putExtra(intentSourceCode, intentSource);
        startActivityForResult(intent, passCityCode);
    }

    // Called when city is saved in DisplayWeatherActivity. Displays saved city in mainActivity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == citySaved) {
            super.onActivityResult(requestCode, resultCode, data);
            try {
                RecyclerView rv = this.findViewById(R.id.recyclerView);
                CustomRecyclerAdapter adapter = (CustomRecyclerAdapter) rv.getAdapter();

                String weatherData = data.getStringExtra("weatherData");
                Complete complete = parseWeatherData(weatherData);

                // Add data to database
                SavedCitiesHelper db = new SavedCitiesHelper(this);
                db.insertData(complete.id);
                db.close();

                adapter.notifyDataSetChanged();
                adapter.notifyItemInserted(adapter.getItemCount());
                adapter.getCompletes().add(complete);

                TextView tv = this.findViewById(R.id.noSavedCities);
                tv.setText("");
                Snackbar.make(findViewById(R.id.root), R.string.snackbar_citySaved_message, BaseTransientBottomBar.LENGTH_LONG).show();
            } catch(IOException e){
                Snackbar.make(findViewById(R.id.root), R.string.snackbar_failure_message, BaseTransientBottomBar.LENGTH_LONG).show();
            } catch(NullPointerException e){
                Snackbar.make(findViewById(R.id.root), R.string.snackbar_failure_message, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }

        if(resultCode == failedSearch) {
            super.onActivityResult(requestCode, resultCode, data);
            Snackbar.make(findViewById(R.id.root), R.string.snackbar_failedSearch_message, BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }

    /////////////////////// LOCATION Funcs /////////////////////////////////////////////////////////

    // if permissions not granted, call requestPermissions().
    // else if location not enabled, call sendToLocationSettings
    // else if permissions/location settings are good, call getLocation
    @SuppressLint("MissingPermission")
    private void initiateLocalWeather(MainActivity context){
        if(!checkPermissions()){
            requestPermissions();
        } else if(!isLocationEnabled()) {
            sendToLocationSettings();
        } else {
            getLocation(context);
        }

    }

    // request Location Permissions
    private void requestPermissions(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, locationPERMISSION_ID);
    }

    //Called when user responds to permissions request
    // If permission granted, check location:
    //      If location enabled: call getLocation
    //      If location disabled: call sendToLocationSettings
    // If permission not granted, display a generic city (todo)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == locationPERMISSION_ID) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                if(isLocationEnabled()){
                    getLocation(this);
                } else {
                    sendToLocationSettings();
                }
            } else{
                //Todo: display a generic city
            }
        }
    }

    //Send to location settings.
    // If location enabled: call getlocation()  Else: display a generic city (todo)
    public void sendToLocationSettings(){
        System.out.println("SENDTOLOCATIONSETTINGS");
        Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    // Get location.
    // If location not null: use it to execute GetLocalWeather
    // If location is null: call (todo)
    public void getLocation(final MainActivity context){
        System.out.println("GETLOCATION");
        FusedLocationProviderClient lastLocationClient = new FusedLocationProviderClient(this); //context not this
        lastLocationClient.getLastLocation().addOnCompleteListener(
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData(context);
                        } else {
                            GetLocalWeather localWeather = new GetLocalWeather(context);
                            localWeather.execute("https://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=9035a183aea3ebf0f7fe2c28dc04c7b3",
                                    "https://api.openweathermap.org/data/2.5/forecast?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=9035a183aea3ebf0f7fe2c28dc04c7b3");
                        }
                    }
                }
        );
    }

    // Return true if permissions granted
    private boolean checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    // Return true if location is enabled
    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    // get new location updates and set onLocationResult callback
    // onLocationResult: If location not null, execute getLocalWeather. Else, display generic city (todo)
    @SuppressLint("MissingPermission")
    private void requestNewLocationData(final MainActivity context){
        System.out.println("REQUESTNEWLOCATIONDATA");
        //Create locationCallback and override onLocationResult to execute getLocalWeather() if location was found
        LocationCallback newLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                System.out.println("ONLOCATIONRESULT");
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    GetLocalWeather localWeather = new GetLocalWeather(context);
                    localWeather.execute("https://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=9035a183aea3ebf0f7fe2c28dc04c7b3",
                            "https://api.openweathermap.org/data/2.5/forecast?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=9035a183aea3ebf0f7fe2c28dc04c7b3");
                } else {
                    //TODO display generic city
                }
            }
        };

        //Make a location request and set interval,priority,etc
        LocationRequest newLocationRequest = new LocationRequest();
        newLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        newLocationRequest.setInterval(100);
        newLocationRequest.setFastestInterval(50);
        newLocationRequest.setNumUpdates(1);

        //Make a fusedLocationProviderClient and call its requestLocationUpdates method with location request, location callback, and Looper
        FusedLocationProviderClient newLocationClient = LocationServices.getFusedLocationProviderClient(context);
        newLocationClient.requestLocationUpdates(newLocationRequest, newLocationCallback, Looper.myLooper());

    }
}
