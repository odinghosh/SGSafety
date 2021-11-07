package com.example.newsgsafety;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * This abstract class represents a hazard manager
 * To add new hazards in the future to the application, need to extend this class
 * This class uses the strategy design pattern, as the checkHazard method will have different
 * implementation depending on the hazard the method is used for
 */

public abstract class HazardManager {
    public String url;
    public HazardManager(String url){
        this.url = url;
    }

    public abstract void checkHazard(Location location, boolean hazardsExposed[], AppCompatActivity activity);
}