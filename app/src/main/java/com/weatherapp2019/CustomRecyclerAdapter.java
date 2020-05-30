package com.weatherapp2019;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.weatherapp2019.JSONClasses.Complete;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {

    private ArrayList<Complete> completes;
    private ClickListener clickListener;
    private Context context;

    public ArrayList<Complete> getCompletes(){
        return this.completes;
    }
    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        TextView cityName;
        TextView temperature;
        TextView temperatureHelper;
        TextView temperatureHelper2;
        TextView min;
        TextView max;
        TextView description;
        ImageView icon;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            itemView.setBackgroundColor(context.getResources().getColor(R.color.backgroundColor));

            cityName = (TextView) itemView.findViewById(R.id.cityName);
            temperature = (TextView) itemView.findViewById(R.id.temperature);
            temperatureHelper = (TextView) itemView.findViewById(R.id.temperatureHelper);
            temperatureHelper2 = (TextView) itemView.findViewById(R.id.temperatureHelper2);
            min = (TextView) itemView.findViewById(R.id.min);
            max = (TextView) itemView.findViewById(R.id.max);
            description = (TextView) itemView.findViewById(R.id.description);
            icon = (ImageView) itemView.findViewById(R.id.weather_icon);
        }

        public void bind(final Complete complete, final ClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(complete);
                }
            });
        }
    }



    // Pass in the contact array into the constructor
    public CustomRecyclerAdapter(ArrayList<Complete> completes, ClickListener clickListener, Context context) {
        this.completes = completes;
        this.clickListener  = clickListener;
        this.context = context;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public CustomRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View cityView = inflater.inflate(R.layout.saved_city_template, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(cityView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(CustomRecyclerAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Complete complete = completes.get(position);

        // Set item views based on your views and data model
        TextView cityName = viewHolder.cityName;
        cityName.setText(complete.name);
        TextView temperature = viewHolder.temperature;
        temperature.setText(String.valueOf(Math.round((complete.main.temp - 273.15) * (9 / 5.0) + 32)));
        TextView temperatureHelper = viewHolder.temperatureHelper;
        temperatureHelper.setText("o");
        TextView temperatureHelper2 = viewHolder.temperatureHelper2;
        temperatureHelper2.setText("F");

        TextView min = viewHolder.min;
        min.setText("Min: " + String.valueOf(Math.round((complete.main.temp_min - 273.15) * (9 / 5.0) + 32)));
        TextView max = viewHolder.max;
        max.setText("Max: " + String.valueOf(Math.round((complete.main.temp_max - 273.15) * (9 / 5.0) + 32)));
        TextView description = viewHolder.description;
        description.setText(complete.weather[0].description);
        setIcon(complete.weather[0].icon, viewHolder.icon);

        viewHolder.bind(complete, clickListener);

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return completes.size();
    }

    public static void setIcon(String icon, ImageView imageView) {
        switch (icon) {
            case "01d":
                imageView.setImageResource(R.drawable.ic_01d);
                break;
            case "01n":
                imageView.setImageResource(R.drawable.ic_01n);
                break;
            case "02d":
                imageView.setImageResource(R.drawable.ic_02d);
                break;
            case "02n":
                imageView.setImageResource(R.drawable.ic_02n);
                break;
            case "03d":
                imageView.setImageResource(R.drawable.ic_03d);
                break;
            case "03n":
                imageView.setImageResource(R.drawable.ic_03n);
                break;
            case "04d":
                imageView.setImageResource(R.drawable.ic_04d);
                break;
            case "04n":
                imageView.setImageResource(R.drawable.ic_04n);
                break;
            case "09d":
                imageView.setImageResource(R.drawable.ic_09d);
                break;
            case "09n":
                imageView.setImageResource(R.drawable.ic_09n);
                break;
            case "10d":
                imageView.setImageResource(R.drawable.ic_10d);
                break;
            case "10n":
                imageView.setImageResource(R.drawable.ic_10n);
                break;
            case "11d":
                imageView.setImageResource(R.drawable.ic_11d);
                break;
            case "11n":
                imageView.setImageResource(R.drawable.ic_11n);
                break;
            case "13d":
                imageView.setImageResource(R.drawable.ic_13d);
                break;
            case "13n":
                imageView.setImageResource(R.drawable.ic_13n);
                break;
            case "50d":
                imageView.setImageResource(R.drawable.ic_50d);
                break;
            case "50n":
                imageView.setImageResource(R.drawable.ic_50n);
                break;
        }
    }
}

