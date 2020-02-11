package com.weatherapp2019.JSONClasses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Rain {
    public double oneh;
    @JsonCreator
    public Rain(@JsonProperty("1h")double oneh){
        this.oneh = oneh;
    }

}
