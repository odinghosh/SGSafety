package com.example.newsgsafety;

import android.content.Intent;
import android.location.Location;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class UvManager extends HazardManager {

    public UvManager(String url, MainActivity mainActivity){
        super(url, mainActivity);
    }


    public void checkHazard(Location location){
        String url = this.url;
        ImageView newButton = this.mainActivity.findViewById(R.id.imageView5);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        ImageView outline = mainActivity.findViewById(R.id.outlineIcon);
                        ImageView shield = mainActivity.findViewById(R.id.shieldIcon);
                        TextView warning = mainActivity.findViewById(R.id.textView3);
                        try {
                            JSONObject status = response.getJSONArray("items").getJSONObject(0).getJSONArray("index").getJSONObject(0); //change to 0 for datetime param
                            int s = status.getInt("value");
                            //s = 10;   //for testing
                            System.out.printf("\ns = %d\n", s);
                            if (s<6){       //healthy UV levels
                                System.out.println("hello world");
                                newButton.setVisibility(View.INVISIBLE);
                                mainActivity.hazardsExposed[0] = false;

                            }else{
                                outline.setActivated(true);
                                shield.setActivated(true);
                                warning.setText("Exposed to hazards");

                                mainActivity.hazardsExposed[0] = true;


                                newButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mainActivity.startActivity(new Intent(mainActivity.getApplicationContext(),UV.class));
                                        mainActivity.saveData();
                                        mainActivity.finish();

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
                        Toast.makeText(mainActivity, "UV code failed", Toast.LENGTH_SHORT);
                    }
                });

        MySingleton.getInstance(mainActivity).addToRequestQueue(jsonObjectRequest);

    }
}
