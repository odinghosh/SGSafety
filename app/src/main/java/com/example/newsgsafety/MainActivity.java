package com.example.newsgsafety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonParser;
import com.google.maps.android.data.geojson.GeoJsonPolygon;


import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback alertLocationCallback;
    private LocationCallback apiLocationCallback;
    private LocationRequest locationRequest;
    private boolean panicSent = false;
    private String panicRequestSent = "";

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String PANIC_REQUEST = "panicLocation";
    public boolean[] boolSettings;
    public boolean[] hazardsExposed;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private ArrayList<HazardManager> hazardManagersList;
    private HazardFactory hazardFactory;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //loadData();
        boolSettings = new boolean[4];
        hazardsExposed = new boolean[4];
        for(int i=0;i<4;i++){
            hazardsExposed[i] = false;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadData();
        System.out.println(boolSettings);
        hazardFactory = new HazardFactory();
        hazardManagersList = new ArrayList<HazardManager>();
        hazardFactory.makeHazardManagers(boolSettings, MainActivity.this, hazardManagersList);

        ImageView outline = findViewById(R.id.outlineIcon);
        ImageView shield = findViewById(R.id.shieldIcon);
        TextView warning = findViewById(R.id.textView3);
        TextView bannerText = findViewById(R.id.description_banner);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        ToggleButton locationSharing = findViewById(R.id.toggleButton);
        if(panicSent){
            locationSharing.toggle();
            bannerText.setText("Location shared to close contacts");
        }


        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);


        alertLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                Location location = locationResult.getLastLocation();
                sendLocationAlert(location);
                fusedLocationClient.removeLocationUpdates(alertLocationCallback);
            }
        };

        apiLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    System.out.println("hello");
                    return;

                }
                System.out.println(boolSettings[0]);
                Location location = locationResult.getLocations().get(0);

                for(int i=0;i<hazardManagersList.size();i++){
                    hazardManagersList.get(i).checkHazard(location, hazardsExposed, MainActivity.this);
                }
                System.out.println(hazardManagersList.size());

                int hazardExposedCount = 0;

                for(int i=0;i<hazardsExposed.length;i++){
                    if(hazardsExposed[i]){
                        hazardExposedCount++;

                    }
                }

                if(hazardExposedCount == 0){
                    shield.setActivated(false);
                    outline.setActivated(false);
                    warning.setText("You are not exposed to any hazards");
                }
            }


                    //fusedLocationClient.removeLocationUpdates(apiLocationCallback);


        };


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED){

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Button panicButton = findViewById(R.id.panicButton);
        panicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.this.panicSent == false) {
                    MainActivity.this.panicSent = true;
                    saveData();
                    startLocationUpdates(alertLocationCallback);

                    locationSharing.toggle();
                    bannerText.setText("Location shared to close contacts");
                }



            }
        });

        Button cancelPanicButton = findViewById(R.id.cancelButton);
        cancelPanicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(panicSent == true){
                    locationSharing.toggle();
                }
                cancelPanicRequest();

                panicSent = false;
                saveData();
                bannerText.setText("Welcome User!");

            }
        });


        final DocumentReference docRef = fStore.collection("users").document(fAuth.getCurrentUser().getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null && value.exists()){
                    checkPanicRequests();
                }
            }
        });


        shield.setActivated(false);
        outline.setActivated(false);
        startLocationUpdates(apiLocationCallback);

    }

    protected void onDestroy(){
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(apiLocationCallback);
    }

    public void cancelPanicRequest(){

        DocumentReference db = fStore.collection("users").document(fAuth.getCurrentUser().getUid());
        db.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> friendList = (ArrayList<String>)document.get("friend_list");
                    String curUser = (String)document.get("username");
                    for(int i=0; i < friendList.size(); i++){
                        String username = friendList.get(i);
                        CollectionReference userList = fStore.collection("users");
                        Query query = userList.whereEqualTo("username", username);
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentReference document = task.getResult().getDocuments().get(0).getReference();
                                    document.update("panic_request",FieldValue.arrayRemove(panicRequestSent));

                                }
                            }
                        });


                    }

                }
            }
        });

    }

    public void main (View view){
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        saveData();
        finish();
    }

    public void contacts (View view){
        startActivity(new Intent(getApplicationContext(),Contacts.class));
        saveData();
        finish();
    }

    public void settings (View view){
        startActivity(new Intent(getApplicationContext(),Settings.class));
        saveData();
        finish();
    }

    public void toggleButtonChange(View view){
        ((ToggleButton)view).toggle();

    }

    private void startLocationUpdates(LocationCallback locationCallback) {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e){

        }
    }

    private void checkPanicRequests(){
        DocumentReference db = fStore.collection("users").document(fAuth.getCurrentUser().getUid());
        LinearLayout ll = (LinearLayout) findViewById(R.id.panicrequests);
        ll.removeAllViews();
        db.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> panicList = (ArrayList<String>)document.get("panic_request");
                    for(int i=0; i < panicList.size(); i++){
                        TextView panicRequest = new TextView(MainActivity.this);
                        panicRequest.setText(panicList.get(i).split(" ")[0]);
                        final String locationString = panicList.get(i);
                        panicRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String panicDetails = locationString;
                                startActivity(new Intent(getApplicationContext(), PanicLocation.class).putExtra("panicDetails",panicDetails));
                                saveData();
                                finish();

                            }
                        });
                        LinearLayout ll = (LinearLayout) findViewById(R.id.panicrequests);
                        ll.addView(panicRequest);
                    }



                }

            }
        });

    }
    private void sendLocationAlert(Location location){
        DocumentReference db = fStore.collection("users").document(fAuth.getCurrentUser().getUid());
        db.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> friendList = (ArrayList<String>)document.get("friend_list");
                    String curUser = (String)document.get("username");
                    panicRequestSent = curUser + " " + location.getLatitude() + " " + location.getLongitude();
                    for(int i=0; i < friendList.size(); i++){
                        String username = friendList.get(i);
                        CollectionReference userList = fStore.collection("users");
                        Query query = userList.whereEqualTo("username", username);
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentReference document = task.getResult().getDocuments().get(0).getReference();
                                    document.update("panic_request",FieldValue.arrayUnion(panicRequestSent));



                                }
                            }
                        });


                    }

                }
            }
        });
    }
    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PANIC_REQUEST, panicRequestSent);
        editor.putBoolean("panic_sent", panicSent);
        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        panicRequestSent   = sharedPreferences.getString(PANIC_REQUEST, "");
        panicSent = sharedPreferences.getBoolean("panic_sent", false);
        boolSettings[0] = sharedPreferences.getBoolean("checkUV", false);
        boolSettings[1] = sharedPreferences.getBoolean("checkFlood", false);
        boolSettings[2] = sharedPreferences.getBoolean("checkTemp", false);
        boolSettings[3] = sharedPreferences.getBoolean("checkDengue", false);

    }
}