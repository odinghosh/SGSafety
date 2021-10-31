package com.example.newsgsafety;

import android.content.Intent;
import android.location.Location;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonParser;
import com.google.maps.android.data.geojson.GeoJsonPolygon;

import org.json.JSONObject;

import java.util.Calendar;

public class DengueManager extends HazardManager {

    public DengueManager(String url, MainActivity mainActivity){
        super(url, mainActivity);
    }
    @Override
    public void checkHazard(Location location) {
        ImageView outline = mainActivity.findViewById(R.id.outlineIcon);
        ImageView shield = mainActivity.findViewById(R.id.shieldIcon);
        TextView warning = mainActivity.findViewById(R.id.textView3);
        ImageView newButton = mainActivity.findViewById(R.id.imageView3);


        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH)+1;
        System.out.println(String.format("%02d",month));



        String url = String.format("https://geo.data.gov.sg/dengue-cluster/2021/%02d/01/geojson/dengue-cluster.geojson",month);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        boolean inDengueArea = false;
                        LinearLayout hazardList = mainActivity.findViewById(R.id.hazardList);

                        //System.out.println(response.toString());
                        GeoJsonParser g = new GeoJsonParser(response);
                        for(GeoJsonFeature feature: g.getFeatures()){
                            LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
                            GeoJsonPolygon gpoly = (GeoJsonPolygon) feature.getGeometry();
                            if (PolyUtil.containsLocation(l,gpoly.getCoordinates().get(0), true)){
                                shield.setActivated(true);
                                outline.setActivated(true);
                                warning.setText("Exposed to hazards");
                                mainActivity.hazardsExposed[3] = true;
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

                                        mainActivity.startActivity(new Intent(mainActivity.getApplicationContext(), Dengue.class).putExtra("cases", numCases));
                                        mainActivity.saveData();
                                        mainActivity.finish();

                                    }
                                });
                                newButton.setVisibility(View.VISIBLE);
                                inDengueArea = true;

                            }


                        }

                        if(!inDengueArea){
                            newButton.setVisibility(View.INVISIBLE);
                            mainActivity.hazardsExposed[3] = false;
                        }






                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mainActivity, "UV code failed", Toast.LENGTH_SHORT);
                    }
                });

        MySingleton.getInstance(mainActivity).addToRequestQueue(jsonObjectRequest);

    }
}
