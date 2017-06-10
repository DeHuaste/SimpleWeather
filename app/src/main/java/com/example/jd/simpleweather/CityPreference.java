package com.example.jd.simpleweather;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by jd on 6/10/17.
 */

public class CityPreference {

    SharedPreferences prefs;

    public CityPreference(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    String getCity(){
        return prefs.getString("city", "Detroit, MI");
    }

    void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }

}
