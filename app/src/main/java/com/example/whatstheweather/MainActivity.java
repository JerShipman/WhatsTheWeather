package com.example.whatstheweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
    SharedPreferences sharedPreferences;

    String currZip;
    double currentTemp;
    double feelsLike;
    double min;
    double max;
    String city;
    String description;

    public void saveZipForLater(){
        sharedPreferences.edit().putString("zip", currZip.toString()).apply();
        sharedPreferences.edit().putString("currentTemp", Double.toString(currentTemp)).apply();
        sharedPreferences.edit().putString("feelsLike", Double.toString(feelsLike)).apply();
        sharedPreferences.edit().putString("min", Double.toString(min)).apply();
        sharedPreferences.edit().putString("max", Double.toString(max)).apply();
    }


    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            String APIKEY = "PUT YOUR API KEY HERE FOR OPEN WEATHER MAP"
            task.execute("https://api.openweathermap.org/data/2.5/weather?zip="+currZip+"&appid="+ APIKEY);
        }
        saveZipForLater();
    }


    public void changeText(){
            tempTextView.setText(String.valueOf(convertToF(currentTemp)));
            feelsLikeTextView.setText(String.valueOf(convertToF(feelsLike)));
            currentConditionsTextView.setText(description);
            cityTextView.setText(city);
            minTextView.setText(String.valueOf(convertToF(min)));
            maxTextView.setText(String.valueOf(convertToF(max)));
    }
    //  sharedPreferences.edit().putString("currentTemp", Double.toString(currentTemp)).apply();
    //        sharedPreferences.edit().putString("feelsLike", Double.toString(feelsLike)).apply();
    //        sharedPreferences.edit().putString("min", Double.toString(min)).apply();
    //        sharedPreferences.edit().putString("max", Double.toString(max)).apply();

    public void updateFromLastSave(){
        currZip = sharedPreferences.getString("zip", "75001");
        feelsLike = Double.parseDouble(sharedPreferences.getString("feelsLike", "0"));
        currentTemp = Double.parseDouble(sharedPreferences.getString("currentTemp", "0"));
        min = Double.parseDouble(sharedPreferences.getString("min", "0"));
        max = Double.parseDouble(sharedPreferences.getString("max", "0"));
        zipCodeTextView.setVisibility(View.INVISIBLE);
        cityTextView.setVisibility(View.VISIBLE);
        submitButton.setVisibility(View.INVISIBLE);
        task = new DownloadTask();
        task.execute("https://api.openweathermap.org/data/2.5/weather?zip="+currZip+"&appid=9209743f1d245e07419cbde41fb9fb94");
        changeText();
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
        sharedPreferences = this.getSharedPreferences("com.example.whatstheweather", Context.MODE_PRIVATE);
        updateFromLastSave();
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
