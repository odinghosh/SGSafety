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

public class UvManager extends HazardManager {

    public UvManager(String url){
        super(url);
    }


    public void checkHazard(Location location, boolean hazardsExposed[], AppCompatActivity activity){


        String url = this.url;
        ImageView newButton = activity.findViewById(R.id.imageView5);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        ImageView outline = activity.findViewById(R.id.outlineIcon);
                        ImageView shield = activity.findViewById(R.id.shieldIcon);
                        TextView warning = activity.findViewById(R.id.textView3);
                        try {
                            JSONObject status = response.getJSONArray("items").getJSONObject(0).getJSONArray("index").getJSONObject(0); //change to 0 for datetime param
                            int s = status.getInt("value");
                            //s = 10;   //for testing
                            System.out.printf("\ns = %d\n", s);
                            if (s<6){       //healthy UV levels
                                System.out.println("hello world");
                                newButton.setVisibility(View.INVISIBLE);
                                hazardsExposed[0] = false;

                            }else{
                                outline.setActivated(true);
                                shield.setActivated(true);
                                warning.setText("Exposed to hazards");

                                hazardsExposed[0] = true;


                                newButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        activity.startActivity(new Intent(activity.getApplicationContext(),UV.class));

                                    }
                                });
                                newButton.setVisibility(View.VISIBLE);


                            }
                        } catch (JSONException e) {
                            //checkUV();
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
