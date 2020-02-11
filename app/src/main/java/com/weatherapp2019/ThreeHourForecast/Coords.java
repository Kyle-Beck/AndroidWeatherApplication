package com.weatherapp2019.ThreeHourForecast;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Coords {
    double lat;
    double lon;


    @JsonCreator
    public Coords(@JsonProperty("lat") double lat, @JsonProperty("lon") double lon) {
        this.lat = lat;
        this.lon = lon;

    }

}
