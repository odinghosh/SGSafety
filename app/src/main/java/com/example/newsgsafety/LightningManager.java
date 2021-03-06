package com.example.newsgsafety;

import android.content.Intent;
import android.location.Location;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class LightningManager extends HazardManager {

    public LightningManager(String url){
        super(url);
    }
    @Override
    public void checkHazard(Location location, boolean hazardsExposed[], AppCompatActivity activity) {

        String url = this.url;
        ImageView newButton = activity.findViewById(R.id.imageView2);
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
                                        activity.startActivity(new Intent(activity.getApplicationContext(), Lightning.class).putExtra("location", inputLocation));


                                    }
                                });
                                newButton.setVisibility(View.VISIBLE);
                                hazardsExposed[1] = true;
                            }else{
                                newButton.setVisibility(View.INVISIBLE);
                                hazardsExposed[1] = false;
                            }
                        } catch (JSONException e) {
                            //checkRain(location);
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
