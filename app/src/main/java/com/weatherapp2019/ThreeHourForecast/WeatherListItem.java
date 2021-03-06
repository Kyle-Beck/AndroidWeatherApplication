package com.weatherapp2019.ThreeHourForecast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class WeatherListItem {
    int id;
    String main;
    String description;
    String icon;

    @JsonCreator
    public WeatherListItem(@JsonProperty("id") int id, @JsonProperty("main") String main,
                   @JsonProperty("description") String description, @JsonProperty("icon") String icon) {
        this.id = id;
        this.main = main;
        this.description = description;
        this.icon = icon;
    }
}
