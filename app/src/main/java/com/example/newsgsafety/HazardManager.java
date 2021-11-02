package com.example.newsgsafety;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public abstract class HazardManager {
    public String url;
    public HazardManager(String url){
        this.url = url;
    }

    public abstract void checkHazard(Location location, boolean hazardsExposed[], AppCompatActivity activity);
}