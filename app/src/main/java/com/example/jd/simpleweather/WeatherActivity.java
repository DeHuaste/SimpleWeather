package com.example.jd.simpleweather;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {
    Typeface weatherFont;
    TextView locationField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherIcon;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        handler = new Handler();
        weatherFont = Typeface.createFromAsset(getAssets(), "weather.ttf");
        locationField = (TextView)findViewById(R.id.location);
        updatedField = (TextView)findViewById(R.id.last_update);
        detailsField = (TextView)findViewById(R.id.details);
        currentTemperatureField = (TextView)findViewById(R.id.temp);
        weatherIcon = (TextView)findViewById(R.id.current_icon);
        weatherIcon.setTypeface(weatherFont);
        updateWeatherData(new CityPreference(this).getCity());

    }


    public void changeCity(String city){

        //gets preferences from activity, opens/editst preferences and modifies city
        this.getPreferences(Activity.MODE_PRIVATE).edit().putString("city", city).commit();

        updateWeatherData(city);

    }

    private void updateWeatherData(final String city) {
        new Thread(){
            public void run(){
                final JSONObject json = RemoteFetch.getJSON(getApplicationContext(), city);
                if(json == null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.place_not_found), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json){
        try {

            locationField.setText(json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");

            detailsField.setText(details.getString("description").toUpperCase(Locale.US) +
                    "\n" + getString(R.string.humidity_label) + " " + main.get("humidity")  + "%" +
                    "\n" + getString(R.string.pressure_label) + " " + main.get("pressure") + " hPa");

            currentTemperatureField.setText(
                    String.format("%.0f", main.getDouble("temp")) + " °F");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
            updatedField.setText(getString(R.string.last_update_label) + " " + updatedOn);

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        } catch (JSONException e) {
        }

    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset){

        int id = actualId / 100;

        String icon = "";

        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime >= sunrise && currentTime < sunset) {
                icon = getResources().getString(R.string.weather_sunny);
            } else {
                icon = getResources().getString(R.string.weather_clear_night);
            }
        } else {
            switch (id){
                case 2 : icon = getResources().getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getResources().getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getResources().getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getResources().getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getResources().getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getResources().getString(R.string.weather_rainy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.weather_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if(item.getItemId() == R.id.change_city) {
            showInputDialog();
        } else if (item.getItemId() == R.id.about) {
            launchAboutActivity();
        } else if (item.getItemId() == R.id.menu_refresh) {
            updateWeatherData(new CityPreference(this).getCity());
        }

        return false;

    }

    private void launchAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void showInputDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_city_dialog_title);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton(R.string.go_button_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });
        builder.show();
    }
}
