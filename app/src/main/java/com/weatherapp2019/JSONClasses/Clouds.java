package com.weatherapp2019.JSONClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Clouds {
    public int all;
    @JsonCreator
    public Clouds(@JsonProperty("all") int all){
        this.all = all;
    }
}
