package com.weatherapp2019.ThreeHourForecast;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class City {
    long id;
    String name;
    public Coords coord;
    String country;
    public long timezone;
    long sunrise;
    long sunset;

    @JsonCreator
    public City(@JsonProperty("id") long id,@JsonProperty("name") String name, @JsonProperty("coord") Coords coord,
    @JsonProperty("country") String country, @JsonProperty("timezone") long timezone, @JsonProperty("sunrise") long sunrise,
                @JsonProperty("sunset") long sunset) {
        this.id = id;
        this.name = name;
        this.coord = coord;
        this.country = country;
        this.timezone = timezone;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }
}