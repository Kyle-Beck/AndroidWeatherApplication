package com.weatherapp2019.JSONClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordinates {
    public double lon, lat;
    @JsonCreator
    public Coordinates(@JsonProperty("lon")double lon, @JsonProperty("lat")double lat){
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }
    public double getLat() {
        return lat;
    }
}
