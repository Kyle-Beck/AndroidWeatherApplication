package com.weatherapp2019;

import com.weatherapp2019.JSONClasses.Complete;
import com.weatherapp2019.ThreeHourForecast.CompleteForecast;

public class CompletePlusForecast {
    public Complete complete;
    public CompleteForecast forecast;
    public CompletePlusForecast(Complete complete, CompleteForecast forecast) {
        this.complete = complete;
        this.forecast = forecast;
    }
}
