package spidey.weather.mevsim.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import spidey.weather.mevsim.Adapters.WeatherAdapter;
import spidey.weather.mevsim.Models.WeatherModel;
import spidey.weather.mevsim.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ArrayList<WeatherModel> weatherModelArrayList;
    WeatherAdapter weatherAdapter;

    LocationManager locationManager;
    int PERMISSION_CODE = 1;
    String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(binding.getRoot());

        weatherModelArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherModelArrayList);
        binding.RVWeathers.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);

        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(cityName);

        binding.searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = Objects.requireNonNull(binding.editCity.getText()).toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter A CityName", Toast.LENGTH_SHORT).show();
                } else {
                    binding.cityName.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please Grant Permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private  String getCityName(double longitude, double latitude){
        String cityName = "Not Found" ;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude, 10);

            for (Address adr : addresses) {
                if (adr != null){
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")){
                        cityName = city;

                    } else {
                        Log.d("TAG","CITY NOT FOUND");
                    }
                }
            }
        } catch (IOException e ){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo (String cityName) {
        String url = "http://api.weatherapi.com/v1/current.json?key=59fe756e53324ef7a99144310223007&q=" + cityName + "&aqi=no";
        binding.cityName.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            binding.loading.setVisibility(View.GONE);
            binding.RLHome.setVisibility(View.VISIBLE);
            weatherModelArrayList.clear();

            try {
                String temper = response.getJSONObject("current").getString("temp_c");
                binding.temperature.setText(temper + "°c");
                int  isDay = response.getJSONObject("current").getInt("is_day");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("http:".concat(conditionIcon)).into(binding.iconWeather);
                binding.weatherCondition.setText(condition);
                if (isDay == 1){
                    //morning
                    Picasso.get().load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ6WJYkhfC8Ef02XujVgVSjSH6g7sZhx7hKbQ&usqp=CAU").into(binding.backGround);
                }else {
                    //night
                    Picasso.get().load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ3v_J2zb0iSh5yuOUYNByYc7XHCWExr5RX6g&usqp=CAU").into(binding.backGround);
                }

                JSONObject forecastObj = response.getJSONObject("forecast");
                JSONObject forecast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                JSONArray hourArray = forecast0.getJSONArray("hour");

                for (int i = 0; i<hourArray.length(); i++){
                    JSONObject hourObj = hourArray.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temperature = hourObj.getString("temp_c");
                    String icon = hourObj.getJSONObject("condition").getString("icon");
                    String windSpeed = hourObj.getString("wind_kph");
                    weatherModelArrayList.add(new WeatherModel(time,temperature, icon, windSpeed));
                }
                weatherAdapter.notifyDataSetChanged();


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please Enter A Valid City Name", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}