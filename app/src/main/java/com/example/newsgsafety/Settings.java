package com.example.newsgsafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class Settings extends AppCompatActivity {

    private Button mSaveBtn;
    private CheckBox mTemp, mDengue, mUltraViolet, mFlood;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String CHECK_TEMP = "checkTemp";
    public static final String CHECK_DENGUE = "checkDengue";
    public static final String CHECK_UV = "checkUV";
    public static final String CHECK_FLOOD = "checkFlood";

    private boolean checkTemp;
    private boolean checkDengue;
    private boolean checkUV;
    private boolean checkFlood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSaveBtn = findViewById(R.id.savebutton);
        mTemp = findViewById(R.id.checkTemp);
        mDengue = findViewById(R.id.checkDengue);
        mUltraViolet = findViewById(R.id.checkUV);
        mFlood = findViewById(R.id.checkFlood);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();

                //Apply changes to the Home Page
            }
        });

        loadData();
        updateViews();
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(CHECK_TEMP, mTemp.isChecked());
        editor.putBoolean(CHECK_DENGUE, mDengue.isChecked());
        editor.putBoolean(CHECK_UV, mUltraViolet.isChecked());
        editor.putBoolean(CHECK_FLOOD, mFlood.isChecked());

        editor.apply();

        Toast.makeText(Settings.this,"Preferences Saved!",Toast.LENGTH_SHORT).show();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        checkTemp = sharedPreferences.getBoolean(CHECK_TEMP, true);
        checkDengue = sharedPreferences.getBoolean(CHECK_DENGUE, true);
        checkUV = sharedPreferences.getBoolean(CHECK_UV, true);
        checkFlood = sharedPreferences.getBoolean(CHECK_FLOOD, true);
    }

    public void updateViews(){
        mTemp.setChecked(checkTemp);
        mDengue.setChecked(checkDengue);
        mUltraViolet.setChecked(checkUV);
        mFlood.setChecked(checkFlood);
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

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }
}