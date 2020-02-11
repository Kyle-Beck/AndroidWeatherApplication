package com.weatherapp2019;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.weatherapp2019.JSONClasses.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class MainActivity extends AppCompatActivity {
    Context context = this;
    LinearLayout ll;
    RecyclerView rv;
    public static CustomRecyclerAdapter adapter;
    FusedLocationProviderClient mFusedLocationClient;
    // These codes are used as keys for the two intent extras
    static final String cityNameCode = "cityNameCode";
    static final String intentSourceCode = "intentSourceCode";
    // Code for passCity intent
    static final int passCityCode = 200;
    final int etID = 22;
    int PERMISSION_ID = 44;
    final ColorDrawable background = new ColorDrawable(Color.RED);
    TextView noSavedCitites;
    LocationCallback locationCallback;
    View localWeatherLayout;
    String localCity;
    public static ArrayList<Complete> result2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add vertical linearLayout to root layout
        ConstraintLayout root = findViewById(R.id.root);
        ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
        root.addView(ll);

        // Add editText box to linearLayout
        EditText et = new EditText(this);
        et.setId(etID);
        et.setHint("Enter City Name Here");

        // et.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ll.addView(et);

        // Add search button to linearLayout that starts DisplayWeatherActivity
        Button search = new Button(this);
        search.setText("Search");
        ll.addView(search);
        search.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                EditText cityName = findViewById(etID);
                passCity(cityName.getText().toString(), "searchedCity");
            }
        });

        LayoutInflater inflater = LayoutInflater.from(context);
        localWeatherLayout = inflater.inflate(R.layout.local_weather_template, null, false);
        localWeatherLayout.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(localCity != null) {
                    passCity(localCity, "searchedCity");
                }

            }
        });
        ll.addView(localWeatherLayout);



        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null) {
                    return;
                }
                for(Location location : locationResult.getLocations()) {
                    System.out.println(location.getLatitude());
                }
            }
        };

        // Request location permissions if needed. Get local and saved weather
        initiateLocalWeather();
        GetSavedWeather getSavedWeather = new GetSavedWeather();
        getSavedWeather.execute("hi");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initiateLocalWeather();
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

    // Take an OpenWeatherMap URL and return a Complete weather object
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

    // Check location permissions and call GetLocalWeather.
    @SuppressLint("MissingPermission")
    private void initiateLocalWeather(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient = new FusedLocationProviderClient(context);
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    LocationRequest locationRequest = new LocationRequest();
                                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                    locationRequest.setInterval(5000);
                                    locationRequest.setFastestInterval(5000);

                                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                                    GetLocalWeather localWeather = new GetLocalWeather();
                                    localWeather.execute("https://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=9035a183aea3ebf0f7fe2c28dc04c7b3",
                                            "https://api.openweathermap.org/data/2.5/forecast?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=9035a183aea3ebf0f7fe2c28dc04c7b3");
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
            if (isLocationEnabled()) {
                mFusedLocationClient = new FusedLocationProviderClient(context);
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    GetLocalWeather localWeather = new GetLocalWeather();
                                    localWeather.execute("https://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=9035a183aea3ebf0f7fe2c28dc04c7b3",
                                            "https://api.openweathermap.org/data/2.5/forecast?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=9035a183aea3ebf0f7fe2c28dc04c7b3");
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
    }

    // Call getComplete. Pass Complete object to buildLocalWeather
    private class GetLocalWeather extends AsyncTask<String, Void, CompletePlusForecast> {
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
            try {
                buildLocalWeather(result.complete, result.forecast);

            } catch(Exception e) {
                // if localCity was never gotten:
                if(localCity == null) {
                    TextView cityName = (TextView) localWeatherLayout.findViewById(R.id.localCityName);
                    cityName.setText("Unable to get local weather data at this time");
                }
                Snackbar.make(findViewById(R.id.root), "Unable to get current, local weather data", BaseTransientBottomBar.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    }

    // Populate localWeatherLayout using Complete parameter.
    private void buildLocalWeather(Complete complete, CompleteForecast forecast) {
        localCity = complete.name;
        // Lookup view from template
        TextView cityName = (TextView) localWeatherLayout.findViewById(R.id.localCityName);
        TextView temperature = (TextView) localWeatherLayout.findViewById(R.id.localTemperature);
        TextView temperatureHelper = (TextView) localWeatherLayout.findViewById(R.id.localTemperatureHelper);
        TextView temperatureHelper2 = (TextView) localWeatherLayout.findViewById(R.id.localTemperatureHelper2);
        TextView minValue = (TextView) localWeatherLayout.findViewById(R.id.minValue);
        TextView maxValue = (TextView) localWeatherLayout.findViewById(R.id.maxValue);
        TextView humidityValue = (TextView) localWeatherLayout.findViewById(R.id.humidityValue);
        TextView feelsLikeValue = (TextView) localWeatherLayout.findViewById(R.id.feelsLikeValue);
        TextView description = (TextView) localWeatherLayout.findViewById(R.id.localDescription);
        ImageView icon = (ImageView) localWeatherLayout.findViewById(R.id.localWeather_icon);
        LinearLayout forecastList = (LinearLayout) localWeatherLayout.findViewById(R.id.forecast_list);

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

    // Returns a textView with given message
    public TextView createMessage(String s) {
        TextView savedWeatherFailure = new TextView(context);
        savedWeatherFailure.setText(s);
        savedWeatherFailure.setGravity(Gravity.CENTER_HORIZONTAL);
        savedWeatherFailure.setTextSize(20);
        savedWeatherFailure.setPadding(10,30,10,30);
        return savedWeatherFailure;
    }

    // Call getComplete for each city in database. Bind Complete array to RecyclerView using CustomRecyclerAdapter.
    private class GetSavedWeather extends AsyncTask<String, Void,  ArrayList<Complete>> {

        // Curse through database making calls to getComplete. Pass Complete list to onPostExecute
        protected  ArrayList<Complete> doInBackground(String... strings) {
            try{
                // Use helper to instantiate database
                SavedCitiesHelper helper = new SavedCitiesHelper(context);
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
            // if result is null, throw nullPointerException. (Because we cant throw exceptions in doInBackground)
            if(result == null) {
                // Make textView if savedWeather retrieval fails
                ll.addView(createMessage("Unable to get saved weather data"));
            } else {
                if(result.size() == 0) {
                    noSavedCitites = createMessage("Add cities to your favorites by searching and clicking the 'Add To Favorites' button");
                    ll.addView(noSavedCitites);
                }
                //Create adapter with result and add it to rv
                adapter = new CustomRecyclerAdapter(result, new ClickListener() {
                    @Override
                    public void onItemClick(Complete complete) {
                        passCity(complete.name, "savedCity");
                    }
                }, context);
                rv = new RecyclerView(context);
                rv.setAdapter(adapter);
                rv.setLayoutManager(new GridLayoutManager(context, 1));


                // Final pointer for touch helper
                result2 = result;

                // Touch helper that adds swipe to delete/ undo snackbar
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback
                        (0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                        int position = viewHolder.getAdapterPosition();
                        final Complete complete = result2.get(position);

                        // Instantiate database
                        final SavedCitiesHelper helper = new SavedCitiesHelper(context);
                        SQLiteDatabase db = helper.getWritableDatabase();

                        // Delete swiped city from database and the adapter's list. Then notify adapter
                        db.delete("SavedCities", "CityID=" + result2.get(position).id, null);
                        result2.remove(position);
                        adapter.notifyItemRemoved(position);
                        if(adapter.getItemCount() == 0) {
                            if(noSavedCitites == null) {
                                noSavedCitites = createMessage("Add cities to your favorites by searching and clicking the 'Add To Favorites' button");
                                ll.addView(noSavedCitites);
                            }
                        }
                        db.close();

                        // Smnackbar notifying delete/offering undo
                        Snackbar snackbarDeleted = Snackbar.make(findViewById(R.id.root), R.string.snackbar_deleted_message, BaseTransientBottomBar.LENGTH_LONG);

                        // Class used to undo
                        class MyUndoListener implements View.OnClickListener {

                            @Override
                            public void onClick(View v) {
                                helper.insertData(complete.id);
                                result2.add(complete);
                                adapter.notifyDataSetChanged();
                                if(noSavedCitites != null) {
                                    ll.removeView(noSavedCitites);
                                    noSavedCitites = null;
                                }
                            }
                        }

                        // Attach undo class to snackbar. Show snackbar
                        snackbarDeleted.setAction("Undo", new MyUndoListener());
                        snackbarDeleted.show();
                    }

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        Toast.makeText(context, "on Move", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        background.setBounds(10000, viewHolder.itemView.getTop(),
                                viewHolder.itemView.getRight() + Math.round(dX), viewHolder.itemView.getBottom());
                        background.draw(c);

                        Drawable icon = ContextCompat.getDrawable(context, R.drawable.trash_can);
                        int itemViewWidth = viewHolder.itemView.getWidth();
                        int itemViewHeight = viewHolder.itemView.getHeight();

                        icon.setBounds(viewHolder.itemView.getRight() - (itemViewWidth / 16) - icon.getIntrinsicWidth() * 3, viewHolder.itemView.getTop() + (itemViewHeight / 4),
                                viewHolder.itemView.getRight() - (itemViewWidth / 16), viewHolder.itemView.getTop() + (itemViewHeight / 4) + icon.getIntrinsicHeight() * 3);
                        icon.draw(c);

                        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                            icon.setVisible(false, true);
                        }

                    }

                });

                // Attach touch helper to rv and rv to ll
                itemTouchHelper.attachToRecyclerView(rv);
                ll.addView(rv);
            }
        }
    }

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
            adapter.notifyDataSetChanged();
            adapter.notifyItemInserted(adapter.getItemCount());
            if(noSavedCitites != null) {
                ll.removeView(noSavedCitites);
                noSavedCitites = null;
            }
            Snackbar.make(findViewById(R.id.root), R.string.snackbar_citySaved_message, BaseTransientBottomBar.LENGTH_LONG).show();
        }
        if(resultCode == failedSearch) {
            super.onActivityResult(requestCode, resultCode, data);
            Snackbar.make(findViewById(R.id.root), R.string.snackbar_failedSearch_message, BaseTransientBottomBar.LENGTH_LONG).show();

        }

    }

    // Used by getLastLocation
    private boolean checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    // Used by getLastLocation
    private void requestPermissions(){
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    // Used by getLastLocation
    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    // Used by getLastLocation
    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Granted. Start getting the location information
            }
        }
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
        }
    };
}
