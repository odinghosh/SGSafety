package com.example.newsgsafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;


public class PanicLocation extends FragmentActivity implements OnMapReadyCallback {




    private GoogleMap mMap;
    //private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panic_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        DocumentReference db = fStore.collection("users").document(fAuth.getCurrentUser().getUid());
        db.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> panic_requests = (ArrayList<String>)document.get("panic_request");
                    double latitude = Double.parseDouble(panic_requests.get(0).split(" ")[0]);
                    double longitude = Double.parseDouble(panic_requests.get(0).split(" ")[1]);
                    LatLng sydney = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Your location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                }
            }
        });
    }
}