package com.weatherapp2019.ThreeHourForecast;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Main {
    public double temp;
    double temp_min;
    double temp_max;
    double pressure;
    double sea_level;
    double grnd_level;
    int humidity;
    double temp_kf;

    @JsonCreator
    public Main(@JsonProperty("temp") double temp, @JsonProperty("temp_min") double temp_min,
                @JsonProperty("temp_max") double temp_max, @JsonProperty("pressure") double pressure,
                @JsonProperty("sea_level") double sea_level, @JsonProperty("grnd_level") double grnd_level,
                @JsonProperty("humidity") int humidity, @JsonProperty("temp_kf") double temp_kf) {
        this.temp = temp;
        this.temp_min = temp_min;
        this.temp_max = temp_max;
        this.pressure = pressure;
        this.sea_level = sea_level;
        this.grnd_level = grnd_level;
        this.humidity = humidity;
        this.temp_kf = temp_kf;
    }

}
