package com.example.newsgsafety;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class TemperatureManager extends HazardManager {

    public TemperatureManager(String url){

        super(url);
    }
    @Override
    public void checkHazard(Location location, boolean hazardsExposed[], AppCompatActivity activity) {
        //startLocationUpdates();
        String url = this.url;
//        LocationResult locationResult = null;
//        Location location = locationResult.getLastLocation();

        ImageView newButton = activity.findViewById(R.id.imageView4);
        ImageView outline = activity.findViewById(R.id.outlineIcon);
        ImageView shield = activity.findViewById(R.id.shieldIcon);
        TextView warning = activity.findViewById(R.id.textView3);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                                hazardsExposed[2] = true;

                                final String inputLocation = location;
                                final Double inputTemp =(closest_forecast);
                                newButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        activity.startActivity(new Intent(activity.getApplicationContext(),Temperature.class).putExtra("location", inputLocation).putExtra("temperature", inputTemp));


                                    }
                                });

                            }else{
                                //System.out.printf("\n1)Area = %s, CLOSEST FORECAST = %s\n", area, closest_forecast); //test
                                //outline.setActivated(false);
                                //shield.setActivated(false);
                                //warning.setText("You are not exposed to any hazards!");
                                newButton.setVisibility(View.INVISIBLE);
                                hazardsExposed[2] = false;
                            }
                        } catch (JSONException e) {
                            //checkTemperature(location);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(mainActivity, "UV code failed", Toast.LENGTH_SHORT);
                    }
                });

        MySingleton.getInstance(activity.getApplicationContext()).addToRequestQueue(jsonObjectRequest);

    }
}
