package com.example.newsgsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Hazards extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hazards);
    }

    public void main (View view){
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }

    public void contacts (View view){
        startActivity(new Intent(getApplicationContext(),Contacts.class));
        finish();
    }

    public void hazards (View view){
        startActivity(new Intent(getApplicationContext(),Hazards.class));
        finish();
    }

    public void settings (View view){
        startActivity(new Intent(getApplicationContext(),Settings.class));
        finish();
    }

    public void temperature (View view){
        startActivity(new Intent(getApplicationContext(),Temperature.class));
        finish();
    }

    public void dengue (View view){
        startActivity(new Intent(getApplicationContext(),Dengue.class));
        finish();
    }

    public void UV (View view){
        startActivity(new Intent(getApplicationContext(),UV.class));
        finish();
    }

    public void flood (View view){
        startActivity(new Intent(getApplicationContext(),Flood.class));
        finish();
    }
}