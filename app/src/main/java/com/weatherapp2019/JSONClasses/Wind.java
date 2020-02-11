package com.weatherapp2019.JSONClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Wind {
    public double speed, deg;
    public int gust;
    @JsonCreator
    public Wind(@JsonProperty("gust") int gust, @JsonProperty("speed") double speed, @JsonProperty("deg") double deg){
        this.speed = speed;
        this.deg = deg;
    }
}
