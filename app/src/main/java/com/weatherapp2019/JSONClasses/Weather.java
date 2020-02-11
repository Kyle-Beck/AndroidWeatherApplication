package com.weatherapp2019.JSONClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Weather{
    public int id;
    public String main;
    public String description;
    public String icon;
    @JsonCreator
    public Weather(@JsonProperty("id")int id, @JsonProperty("main") String main, @JsonProperty("description") String description, @JsonProperty("icon") String icon){
        this.id = id;
        this.main = main;
        this.description = description;
        this.icon = icon;
    }
}
