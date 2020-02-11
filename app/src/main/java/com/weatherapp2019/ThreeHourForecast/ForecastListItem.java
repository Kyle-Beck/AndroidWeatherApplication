package com.weatherapp2019.ThreeHourForecast;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastListItem {
    public long dt;
    public Main main;
    public Weather[] weather;

    @JsonCreator
    public ForecastListItem(@JsonProperty("dt") long dt, @JsonProperty("main") Main main,
                            @JsonProperty("weather") Weather[] weather) {
        this.dt = dt;
        this.main = main;
        this.weather = weather;
    }

}
