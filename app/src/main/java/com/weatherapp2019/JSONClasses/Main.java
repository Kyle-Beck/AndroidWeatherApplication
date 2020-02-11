package com.weatherapp2019.JSONClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Main {
    public double temp;
    public double feels_like;
    public int pressure;
    public int humidity;
    public double temp_min;
    public double temp_max;
    @JsonCreator
    public Main(@JsonProperty("temp") double temp, @JsonProperty("feels_like") double feels_like, @JsonProperty("pressure") int pressure,@JsonProperty("humidity") int humidity,
                @JsonProperty("temp_min") double temp_min,@JsonProperty("temp_max") double temp_max){
        this.temp = temp;
        this.feels_like = feels_like;
        this.pressure = pressure;
        this.humidity = humidity;
        this.temp_min = temp_min;
        this.temp_max = temp_max;
    }
}
