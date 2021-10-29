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
import android.os.Handler;
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
    private Location lastLocation;
    private Handler locationHandler;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String PANIC_REQUEST = "panicLocation";
    public boolean[] boolSettings = {false, false, false, false};  //idx 0 = UV, idx 1 = flood, idx 2 = dengue, idx 3 = temperature
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private UvManager uvManager = new UvManager("https://api.data.gov.sg/v1/environment/uv-index", MainActivity.this);
    private TemperatureManager temperatureManager = new TemperatureManager("https://api.data.gov.sg/v1/environment/air-temperature",MainActivity.this);
    private LightningManager lightningManager = new LightningManager("https://api.data.gov.sg/v1/environment/2-hour-weather-forecast", MainActivity.this);
    private DengueManager dengueManager = new DengueManager("https://geo.data.gov.sg/dengue-cluster/2021/10/01/geojson/dengue-cluster.geojson", MainActivity.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadData();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ImageView outline = findViewById(R.id.outlineIcon);
        ImageView shield = findViewById(R.id.shieldIcon);
        TextView warning = findViewById(R.id.textView3);
        TextView bannerText = findViewById(R.id.description_banner);
        locationHandler = new Handler();
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



                //sendLocationAlert(location);
                MainActivity.this.lastLocation = location;


                locationHandler.postDelayed(new Runnable() {
                    public void run(){
                        locationHandler.removeCallbacksAndMessages(null);
                        fusedLocationClient.removeLocationUpdates(alertLocationCallback);
                        //hazardList.setVisibility(View.VISIBLE);
                        sendLocationAlert(lastLocation);

                    }

                }, 2000);








                //fusedLocationClient.removeLocationUpdates(alertLocationCallback);
            }
        };

        apiLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    System.out.println("hello");
                    return;

                }
                Location location = locationResult.getLocations().get(0);


                //checkUV();
                uvManager.checkHazard(location);
                lightningManager.checkHazard(location);
                dengueManager.checkHazard(location);

                //checkDengue(location);
                //checkTemperature(location);
                temperatureManager.checkHazard(location);

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
                //if(MainActivity.this.panicSent == false) {
                    MainActivity.this.panicSent = true;
                    //startLocationUpdates(apiLocationCallback);
                    //cancelPanicRequest();
                    startLocationUpdates(alertLocationCallback);

                    locationSharing.toggle();
                    bannerText.setText("Location shared to close contacts");
                //}



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

    public void checkUV(){
        String url = "https://api.data.gov.sg/v1/environment/uv-index";
        ImageView newButton = findViewById(R.id.imageView5);
        if(!boolSettings[0]){
            newButton.setVisibility(View.INVISIBLE);
            return;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        ImageView outline = findViewById(R.id.outlineIcon);
                        ImageView shield = findViewById(R.id.shieldIcon);
                        TextView warning = findViewById(R.id.textView3);
                        try {
                            JSONObject status = response.getJSONArray("items").getJSONObject(0).getJSONArray("index").getJSONObject(0); //change to 0 for datetime param
                            int s = status.getInt("value");
                            //s = 10;   //for testing
                            System.out.printf("\ns = %d\n", s);
                            if (s<6){       //healthy UV levels
                                System.out.println("hello world");
                                newButton.setVisibility(View.INVISIBLE);

                            }else{
                                outline.setActivated(true);
                                shield.setActivated(true);
                                warning.setText("Exposed to hazards");


                                newButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        startActivity(new Intent(getApplicationContext(),UV.class));
                                        saveData();
                                        finish();

                                    }
                                });
                                newButton.setVisibility(View.VISIBLE);


                            }
                        } catch (JSONException e) {
                            checkUV();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "UV code failed", Toast.LENGTH_SHORT);
                    }
                });

        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);
    }
    public void checkRain(Location location){

        String url = "https://api.data.gov.sg/v1/environment/2-hour-weather-forecast";


        ImageView newButton = findViewById(R.id.imageView2);
        if(!boolSettings[1]){
            newButton.setVisibility(View.INVISIBLE);
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ImageView outline = findViewById(R.id.outlineIcon);
                        ImageView shield = findViewById(R.id.shieldIcon);
                        TextView warning = findViewById(R.id.textView3);


                        try {
                            double min_dist = 100000;;
                            int index = 0;

                            //TESTING
                            double temp_lat = location.getLatitude();
                            double temp_lon = location.getLongitude();

                            for (int i = 0; i < 46; i++) {
                                //System.out.printf("i = %d\n", i);
                                String forecast = response.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts").getJSONObject(i).getString("forecast");
                                String area = response.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts").getJSONObject(i).getString("area");
                                double lat = response.getJSONArray("area_metadata").getJSONObject(i).getJSONObject("label_location").getDouble("latitude");
                                double lon = response.getJSONArray("area_metadata").getJSONObject(i).getJSONObject("label_location").getDouble("longitude");
                                //s = 10;   //for testing
                                //System.out.printf("\narea = %s, latitude = %f, longitude = %f, forecast = %s\n", area, lat, lon, forecast);

                                if (Math.abs(temp_lat - lat) + Math.abs(temp_lon - lon) < min_dist){ //find closest location to user
                                    min_dist = Math.abs(temp_lat - lat) + Math.abs(temp_lon - lon);
                                    index = i;
                                }
                            }
                            String closest_forecast = response.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts").getJSONObject(index).getString("forecast");
                            String area = response.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts").getJSONObject(index).getString("area");
                            //closest_forecast = "Thundery Showers";    //test
                            if (closest_forecast.equals("Thundery Showers")){
                                System.out.printf("\n1)Area = %s, CLOSEST FORECAST = %s\n", area, closest_forecast); //test
                                outline.setActivated(true);
                                shield.setActivated(true);
                                warning.setText("Exposed to hazards");
                                final String inputLocation = area;
                                newButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        startActivity(new Intent(getApplicationContext(),Flood.class).putExtra("location", inputLocation));
                                        saveData();
                                        finish();

                                    }
                                });
                                newButton.setVisibility(View.VISIBLE);
                            }else{
                                newButton.setVisibility(View.INVISIBLE);
                            }
                        } catch (JSONException e) {
                            checkRain(location);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "UV code failed", Toast.LENGTH_SHORT);
                    }
                });

        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);
    }




    public void checkTemperature(Location location){
        //startLocationUpdates();
        String url = "https://api.data.gov.sg/v1/environment/air-temperature";
//        LocationResult locationResult = null;
//        Location location = locationResult.getLastLocation();

        ImageView newButton = findViewById(R.id.imageView4);
        if(!boolSettings[2]){
            newButton.setVisibility(View.INVISIBLE);
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ImageView outline = findViewById(R.id.outlineIcon);
                        ImageView shield = findViewById(R.id.shieldIcon);
                        TextView warning = findViewById(R.id.textView3);

                        try {
                            double min_dist = 100000;;
                            int index = 0;

                            //TESTING
                            double temp_lat = location.getLatitude();
                            double temp_lon = location.getLongitude();
                            int len = response.getJSONArray("items").getJSONObject(0).getJSONArray("readings").length();
                            System.out.println(len);

                            for (int i = 0; i < len; i++) {
                                //System.out.printf("i = %d\n", i);
                                //String forecast = response.getJSONArray("items").getJSONObject(0).getJSONArray("readings").getJSONObject(i).getString("value");
                                String area = response.getJSONArray("items").getJSONObject(0).getJSONArray("readings").getJSONObject(i).getString("station_id");
                                System.out.println(area);
                                double lat = response.getJSONObject("metadata").getJSONArray("stations").getJSONObject(i).getJSONObject("location").getDouble("latitude");
                                double lon = response.getJSONObject("metadata").getJSONArray("stations").getJSONObject(i).getJSONObject("location").getDouble("longitude");
                                //s = 10;   //for testing
                                //System.out.printf("\narea = %s, latitude = %f, longitude = %f, forecast = %s\n", area, lat, lon, forecast);

                                if (Math.abs(temp_lat - lat) + Math.abs(temp_lon - lon) < min_dist){ //find closest location to user
                                    min_dist = Math.abs(temp_lat - lat) + Math.abs(temp_lon - lon);
                                    index = i;
                                }
                            }
                            double closest_forecast = response.getJSONArray("items").getJSONObject(0).getJSONArray("readings").getJSONObject(index).getDouble("value");
                            String area = response.getJSONArray("items").getJSONObject(0).getJSONArray("readings").getJSONObject(index).getString("station_id");
                            //closest_forecast = 35;    //test
                            String location = "";
                            for (int i = 0; i < len; i++) {
                                //System.out.printf("i = %d\n", i);
                                //String forecast = response.getJSONArray("items").getJSONObject(0).getJSONArray("readings").getJSONObject(i).getString("value");
                                //String area = response.getJSONArray("items").getJSONObject(0).getJSONArray("readings").getJSONObject(i).getString("station_id");
                                String id = response.getJSONObject("metadata").getJSONArray("stations").getJSONObject(i).getString("id");
                                location  = response.getJSONObject("metadata").getJSONArray("stations").getJSONObject(i).getString("name");
                                if(id.equals(area)){
                                    break;
                                }
                                //s = 10;   //for testing
                                //System.out.printf("\narea = %s, latitude = %f, longitude = %f, forecast = %s\n", area, lat, lon, forecast);
                            }
                            System.out.println(location);
                            if (closest_forecast > 25){
                                //System.out.printf("\n1)Area = %s, CLOSEST FORECAST = %s\n", area, closest_forecast); //test
                                System.out.println(area);
                                outline.setActivated(true);
                                shield.setActivated(true);
                                warning.setText("Exposed to hazards");
                                newButton.setVisibility(View.VISIBLE);

                                final String inputLocation = location;
                                final Double inputTemp =(closest_forecast);
                                newButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        startActivity(new Intent(getApplicationContext(),Temperature.class).putExtra("location", inputLocation).putExtra("temperature", inputTemp));
                                        saveData();
                                        finish();

                                    }
                                });

                            }else{
                                //System.out.printf("\n1)Area = %s, CLOSEST FORECAST = %s\n", area, closest_forecast); //test
                                //outline.setActivated(false);
                                //shield.setActivated(false);
                                //warning.setText("You are not exposed to any hazards!");
                                newButton.setVisibility(View.INVISIBLE);
                            }
                        } catch (JSONException e) {
                            checkTemperature(location);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "UV code failed", Toast.LENGTH_SHORT);
                    }
                });

        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);
    }





    public void checkDengue(Location location){

        ImageView outline = findViewById(R.id.outlineIcon);
        ImageView shield = findViewById(R.id.shieldIcon);
        TextView warning = findViewById(R.id.textView3);
        ImageView newButton = findViewById(R.id.imageView3);
        if(!boolSettings[3]){
            newButton.setVisibility(View.INVISIBLE);
            return;
        }



        String url = "https://geo.data.gov.sg/dengue-cluster/2021/10/01/geojson/dengue-cluster.geojson";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        boolean inDengueArea = false;
                        LinearLayout hazardList = findViewById(R.id.hazardList);

                        //System.out.println(response.toString());
                        GeoJsonParser g = new GeoJsonParser(response);
                        for(GeoJsonFeature feature: g.getFeatures()){
                            LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
                            GeoJsonPolygon gpoly = (GeoJsonPolygon) feature.getGeometry();
                            if (PolyUtil.containsLocation(l,gpoly.getCoordinates().get(0), true)){
                                shield.setActivated(true);
                                outline.setActivated(true);
                                warning.setText("Exposed to hazards");
                                String area = feature.getProperty("Description");
                                int i;
                                int j = area.indexOf("</td>");
                                //String locationArea = area.substring(i+4, j);
                                String numArea = area.substring(j + 5);
                                i = numArea.indexOf("<td>");
                                j = numArea.indexOf("</td>");
                                numArea = numArea.substring(i+4, j);
                                int numCases = Integer.parseInt(numArea);
                                newButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        startActivity(new Intent(getApplicationContext(), Dengue.class).putExtra("cases", numCases));
                                        saveData();
                                        finish();

                                    }
                                });
                                newButton.setVisibility(View.VISIBLE);
                                inDengueArea = true;

                            }


                        }

                        if(!inDengueArea){
                            newButton.setVisibility(View.INVISIBLE);
                        }






                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "UV code failed", Toast.LENGTH_SHORT);
                    }
                });

        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);

    }


}