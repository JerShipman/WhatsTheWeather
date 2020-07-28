package com.example.whatstheweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {
    TextView tempTextView;
    TextView zipCodeTextView;
    TextView cityTextView;
    TextView feelsLikeTextView;
    TextView currentConditionsTextView;
    TextView minTextView;
    TextView maxTextView;
    Button submitButton;
    DownloadTask task;
    LocationManager locationManager;

    String currZip;
    double currentTemp;
    double feelsLike;
    double min;
    double max;
    String city;
    String description;

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void getUserLocation(View view){
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }


    public int convertToF(double k){
        int returnF = (int) ((k - 273.15)*9/5 + 32);
        return returnF;
    }


    public void changeCity(View view) {
        cityTextView.setVisibility(View.INVISIBLE);
        zipCodeTextView.setVisibility(View.VISIBLE);
        zipCodeTextView.setText(currZip);
        submitButton.setVisibility(View.VISIBLE);
    }

    public void submitZip(View view){
        closeKeyboard();
        if(zipCodeTextView.getText().length() > 4){
            currZip = zipCodeTextView.getText().toString();
            zipCodeTextView.setVisibility(View.INVISIBLE);
            cityTextView.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.INVISIBLE);
            task = new DownloadTask();
            task.execute("https://api.openweathermap.org/data/2.5/weather?zip="+currZip+"&appid=f7397aca7a9bbd3ed18cd930c6f9e5df");
        }
    }

    public void changeText(){
            tempTextView.setText(String.valueOf(convertToF(currentTemp)));
            feelsLikeTextView.setText(String.valueOf(convertToF(feelsLike)));
            currentConditionsTextView.setText(description);
            cityTextView.setText(city);
            minTextView.setText(String.valueOf(convertToF(min)));
            maxTextView.setText(String.valueOf(convertToF(max)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zipCodeTextView = findViewById(R.id.zipCodeTextView);
        cityTextView = findViewById(R.id.cityTextView);
        submitButton = findViewById(R.id.submitButton);
        tempTextView = findViewById(R.id.tempTextView);
        feelsLikeTextView = findViewById(R.id.feelsLikeTextView);
        minTextView = findViewById(R.id.minTextView);
        maxTextView = findViewById(R.id.maxTextView);
        currentConditionsTextView = findViewById(R.id.currentConditionsTextView);
        cityTextView.setVisibility(View.INVISIBLE);
        description = "";
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;

            HttpURLConnection urlConnection = null;
            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {//s is result
            if(s==null){
                Toast.makeText(getApplicationContext(), "Uh-oh, do you have the right zip?", Toast.LENGTH_SHORT).show();
                return;
            }
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String weatherInfo = jsonObject.getString("weather");
                String mainInfo = jsonObject.getString("main");//has temp and feels like
                city = jsonObject.getString("name");

                JSONObject mainInfoArray = new JSONObject(mainInfo);
                currentTemp = mainInfoArray.getDouble("temp");
                feelsLike = mainInfoArray.getDouble("feels_like");
                min = mainInfoArray.getDouble("temp_min");
                max = mainInfoArray.getDouble("temp_max");

                JSONArray weatherArr = new JSONArray(weatherInfo);
                for (int i = 0; i < weatherArr.length(); i++) {
                    JSONObject jsonpart = weatherArr.getJSONObject(i);
                    description = jsonpart.getString("description");
                };

                changeText();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}