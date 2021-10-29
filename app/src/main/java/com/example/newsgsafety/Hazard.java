package com.example.newsgsafety;

import android.location.Location;

import androidx.core.app.ActivityCompat;

public abstract class Hazard{
    public String url;
    public MainActivity mainActivity;
    public boolean enabled;
    public Hazard(String url, MainActivity mainActivity){
        this.url = url;
        this.mainActivity = mainActivity;

    }

    public abstract void checkHazard(Location location);
}