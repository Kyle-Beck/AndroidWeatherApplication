package com.weatherapp2019.ThreeHourForecast;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteForecast {
    String cod;
    double message;
    int cnt;
    public ForecastListItem[] list;
    public City city;

    @JsonCreator
    public CompleteForecast(@JsonProperty("cod") String cod, @JsonProperty("message") double message,
                            @JsonProperty("cnt") int cnt, @JsonProperty("list") ForecastListItem[] list,
                            @JsonProperty("city") City city) {
        this.cod = cod;
        this.message = message;
        this.cnt = cnt;
        this.list = list;
        this.city = city;
    }
}
