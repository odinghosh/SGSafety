package com.example.newsgsafety;
import java.util.ArrayList;

public class HazardFactory {

    public void makeHazardManagers(boolean[] boolSettings, MainActivity mainActivity, ArrayList<HazardManager> hazardManagersList){
        for(int i=0;i<4;i++){
            if(boolSettings[i] == true){
                System.out.println("true");
                switch(i){
                    case 0:
                        hazardManagersList.add(new UvManager("https://api.data.gov.sg/v1/environment/uv-index"));
                        break;
                    case 1:
                        hazardManagersList.add(new LightningManager("https://api.data.gov.sg/v1/environment/2-hour-weather-forecast"));
                        break;
                    case 3:
                        hazardManagersList.add(new DengueManager("https://geo.data.gov.sg/dengue-cluster/2021/10/01/geojson/dengue-cluster.geojson"));
                        break;
                    case 2:
                        hazardManagersList.add(new TemperatureManager("https://api.data.gov.sg/v1/environment/air-temperature"));
                        break;


                }
            }
        }

        //return hazardManagersList;

    }
}
