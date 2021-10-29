package com.example.newsgsafety;
import java.util.ArrayList;

public class HazardFactory {

    public void makeHazardManagers(boolean[] boolSettings, MainActivity mainActivity, ArrayList<HazardManager> hazardManagersList){
        for(int i=0;i<boolSettings.length;i++){
            if(boolSettings[i]){
                switch(i){
                    case 0:
                        hazardManagersList.add(new UvManager("https://api.data.gov.sg/v1/environment/uv-index", mainActivity));
                        break;
                    case 1:
                        hazardManagersList.add(new LightningManager("https://api.data.gov.sg/v1/environment/2-hour-weather-forecast", mainActivity));
                        break;
                    case 2:
                        hazardManagersList.add(new DengueManager("https://geo.data.gov.sg/dengue-cluster/2021/10/01/geojson/dengue-cluster.geojson", mainActivity));
                        break;
                    case 3:
                        hazardManagersList.add(new TemperatureManager("https://api.data.gov.sg/v1/environment/air-temperature", mainActivity));
                        break;


                }
            }
        }

        //return hazardManagersList;

    }
}
