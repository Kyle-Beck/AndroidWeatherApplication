package com.weatherapp2019;

import android.view.View;
import android.widget.Button;

import com.weatherapp2019.JSONClasses.Complete;

public class PassLocalCityClickListener implements Button.OnClickListener {
    MainActivity context;
    Complete complete;
    public PassLocalCityClickListener(MainActivity context, Complete complete){
        super();
        this.context = context;
        this.complete = complete;
    }
    @Override
    public void onClick(View v) {
        context.passCity(complete.name, "searchedCity");
    }
}
