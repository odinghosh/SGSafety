package com.example.newsgsafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        locationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                Location location = locationResult.getLastLocation();
                sendLocationAlert(location);
                fusedLocationClient.removeLocationUpdates(locationCallback);
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
                startLocationUpdates();



            }
        });

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

    public void paniclocation (View view){
        startActivity(new Intent(getApplicationContext(),PanicLocation.class));
        finish();
    }

    private void startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e){

        }
    }

    private void sendLocationAlert(Location location){
        DocumentReference db = fStore.collection("users").document(fAuth.getCurrentUser().getUid());
        db.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> friendList = (ArrayList<String>)document.get("friend_list");
                    for(int i=0; i < friendList.size(); i++){
                        String username = friendList.get(i);
                        CollectionReference userList = fStore.collection("users");
                        Query query = userList.whereEqualTo("username", username);
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentReference document = task.getResult().getDocuments().get(0).getReference();
                                    document.update("panic_request", location.getLatitude());



                                }
                            }
                        });


                    }

                }
            }
        });
    }
}