package com.example.newsgsafety;

import static java.lang.Double.POSITIVE_INFINITY;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonParser;
import com.google.maps.android.data.geojson.GeoJsonPolygon;


import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback alertLocationCallback;
    private LocationCallback apiLocationCallback;
    private LocationRequest locationRequest;
    private boolean panicSent = false;
    private String panicRequestSent = "";
    private Location locationData;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String PANIC_REQUEST = "panicLocation";
    private boolean[] bool_arr = {false, false, false, false};  //idx 0 = UV, idx 1 = flood, idx 2 = dengue, idx 3 = temperature

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadData();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView outline = findViewById(R.id.outlineIcon);
        ImageView shield = findViewById(R.id.shieldIcon);
        TextView warning = findViewById(R.id.textView3);


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        ToggleButton locationSharing = findViewById(R.id.toggleButton);


        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        //startLocationUpdates();

        alertLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                Location location = locationResult.getLastLocation();
                MainActivity.this.locationData = location;


                sendLocationAlert(location);



                fusedLocationClient.removeLocationUpdates(alertLocationCallback);
            }
        };

        apiLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                Location location = locationResult.getLastLocation();
                MainActivity.this.locationData = location;


                checkUV();
                checkRain(location);
                checkDengue(location);


                fusedLocationClient.removeLocationUpdates(apiLocationCallback);
            }
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
                    startLocationUpdates(alertLocationCallback);

                    locationSharing.toggle();
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

                panicSent = false;

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

        //checkUV();
        //checkRain();
        //checkDengue();
        shield.setActivated(false);
        outline.setActivated(false);
        LinearLayout hazardList = findViewById(R.id.hazardList);
        hazardList.removeAllViews();
        startLocationUpdates(apiLocationCallback);

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

    public void hazards (View view){
        startActivity(new Intent(getApplicationContext(),Hazards.class));
        saveData();
        finish();
    }

    public void settings (View view){
        startActivity(new Intent(getApplicationContext(),Settings.class));
        saveData();
        finish();
    }

    public void paniclocation (View view){
        startActivity(new Intent(getApplicationContext(),PanicLocation.class));
        finish();
    }

    private void startLocationUpdates(LocationCallback locationCallback) {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
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
                        panicRequest.setText(panicList.get(i));
                        panicRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String panicDetails = ((TextView)view).getText().toString();
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
        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        panicRequestSent   = sharedPreferences.getString(PANIC_REQUEST, "");
    }

    public void checkUV(){
        String url = "https://api.data.gov.sg/v1/environment/uv-index";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        ImageView outline = findViewById(R.id.outlineIcon);
                        ImageView shield = findViewById(R.id.shieldIcon);
                        TextView warning = findViewById(R.id.textView3);
                        try {
                            JSONObject status = response.getJSONArray("items").getJSONObject(0).getJSONArray("index").getJSONObject(1); //change to 0 for datetime param
                            int s = status.getInt("value");
                            s = 10;   //for testing
                            System.out.printf("\ns = %d\n", s);
                            if (s<6){       //healthy UV levels
                                //outline.setActivated(false);
                                //shield.setActivated(false);
                                //warning.setText("You are not exposed to any hazards!");
                            }else{
                                outline.setActivated(true);
                                shield.setActivated(true);
                                warning.setText("WARNING! Unhealthy UV levels!");
                                MainActivity.this.bool_arr[0] = true;
                                LinearLayout hazardList = findViewById(R.id.hazardList);
                                ImageView newButton = new ImageView(MainActivity.this);
                                newButton.setImageResource(R.drawable.ultraviolet_icon);
                                newButton.setAdjustViewBounds(true);
                                newButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        startActivity(new Intent(getApplicationContext(),UV.class));
                                        saveData();
                                        finish();

                                    }
                                });
                                hazardList.addView(newButton);


                            }
                        } catch (JSONException e) {
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
        //startLocationUpdates();
        String url = "https://api.data.gov.sg/v1/environment/2-hour-weather-forecast";
//        LocationResult locationResult = null;
//        Location location = locationResult.getLastLocation();

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
                                System.out.printf("\narea = %s, latitude = %f, longitude = %f, forecast = %s\n", area, lat, lon, forecast);

                                if (Math.abs(temp_lat - lat) + Math.abs(temp_lon - lon) < min_dist){ //find closest location to user
                                    min_dist = Math.abs(temp_lat - lat) + Math.abs(temp_lon - lon);
                                    index = i;
                                }
                            }
                            String closest_forecast = response.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts").getJSONObject(index).getString("forecast");
                            String area = response.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts").getJSONObject(index).getString("area");
                            closest_forecast = "Thundery Showers";    //test
                            if (closest_forecast.equals("Thundery Showers")){
                                System.out.printf("\n1)Area = %s, CLOSEST FORECAST = %s\n", area, closest_forecast); //test
                                outline.setActivated(true);
                                shield.setActivated(true);
                                warning.setText("WARNING! High chance of lightning and flooding!");
                                MainActivity.this.bool_arr[1] = true;

                                LinearLayout hazardList = findViewById(R.id.hazardList);
                                ImageView newButton = new ImageView(MainActivity.this);
                                newButton.setImageResource(R.drawable.flooding_icon);
                                newButton.setAdjustViewBounds(true);
                                newButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        startActivity(new Intent(getApplicationContext(),Flood.class));
                                        saveData();
                                        finish();

                                    }
                                });
                                hazardList.addView(newButton);
                            }else{
                                //System.out.printf("\n1)Area = %s, CLOSEST FORECAST = %s\n", area, closest_forecast); //test
                                //outline.setActivated(false);
                                //shield.setActivated(false);
                                //warning.setText("You are not exposed to any hazards!");
                            }
                        } catch (JSONException e) {
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


        String url = "https://geo.data.gov.sg/dengue-cluster/2021/10/01/geojson/dengue-cluster.geojson";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        GeoJsonParser g = new GeoJsonParser(response);
                        for(GeoJsonFeature feature: g.getFeatures()){
                            LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
                            GeoJsonPolygon gpoly = (GeoJsonPolygon) feature.getGeometry();
                            if (PolyUtil.containsLocation(l,gpoly.getCoordinates().get(0), true)){
                                shield.setActivated(true);
                                outline.setActivated(true);
                                warning.setText("Exposed to dengue");

                            }


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