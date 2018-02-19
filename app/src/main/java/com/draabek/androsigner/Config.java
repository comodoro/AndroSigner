package com.draabek.androsigner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * Global configuration for the app
 * Created by Vojtech Drabek on 2018-02-04.
 */

public class Config {

    private String endpoint;

    public Config(String json) {
        Gson gson = new Gson();
        Map<String, String> options = gson.fromJson(json, new TypeToken<Map<String, String>>() { }.getType());
        if (options.get("endpoint") != null) {
            endpoint = options.get("endpoint");
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
