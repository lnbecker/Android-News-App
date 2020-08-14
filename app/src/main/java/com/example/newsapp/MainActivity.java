package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.Manifest;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {
    Toolbar toolbar;
    LocationManager locationManager;
    String provider;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private double latitude;
    private double longitude;
    private String city;
    private String state;
    private String weatherDescription;
    private String temp;
    private String query;
    ArrayList<String> suggestions;
    private SearchView searchView;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> suggestionsAdapter;
    private CardView weatherCard;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set back to main theme after launch screen
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting toolbar
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        weatherCard = findViewById(R.id.weatherCard);
        weatherCard.setVisibility(View.GONE);


        //Navigation menu listener
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        break;
                    case R.id.action_headlines:
                        Intent headlines = new Intent(MainActivity.this,TopHeadlinesActivity.class);
                        startActivity(headlines);
                        break;
                    case R.id.action_trending:
                        Intent trending = new Intent(MainActivity.this,TrendingActivity.class);
                        startActivity(trending);
                        break;
                    case R.id.action_bookmarks:
                        Intent bookmarks = new Intent(MainActivity.this,BookmarksActivity.class);
                        startActivity(bookmarks);
                        break;
                }
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);


        //Weather stuff
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        checkLocationPermission();

        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        }
        else {
        }

    }

    //For adding search feature to app bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);

        suggestions = new ArrayList<>();


        // Get the SearchView and set the searchable configuration
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new
                ComponentName(this,SearchActivity.class)));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        int autoCompleteId = searchView.getResources().getIdentifier("android:id/search_src_text", null, null);
        autoCompleteTextView = (AutoCompleteTextView) searchView.findViewById(autoCompleteId);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                autoCompleteTextView.setText("" + queryString);
            }
        });

        try {
            getSuggestions(query, new VolleyBlankCallback(){
                @Override
                public void onSuccess() {
                    //mSuggestions = suggestions.toArray(new String[0]);
                    setUpViews();
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }


    public void setUpViews(){
        // Create a new ArrayAdapter and add data to search auto complete object.
        suggestionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        autoCompleteTextView.setAdapter(suggestionsAdapter);
        // Listen to search view item on click event.
        final Context context = this;

        // Below event is triggered when submit search query.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    getSuggestions(newText, new VolleyBlankCallback(){
                        @Override
                        public void onSuccess() {
                            suggestionsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, suggestions);
                            autoCompleteTextView.setAdapter(suggestionsAdapter);
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
    }


    private void getSuggestions(String query, final VolleyBlankCallback callback) throws UnsupportedEncodingException, JSONException {
        this.query = query;
        final String api_key = "edfe748ad8ff4844a4c39472d2cd5c2b";
        String encodedQuery;
        if(query == null || query.isEmpty()){
            encodedQuery = "";
        }
        else {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
        }
        String url = "https://laurenbecker-4.cognitiveservices.azure.com/bing/v7.0/suggestions?q=";
        url += encodedQuery;
        JSONObject params = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<String> retrievedSuggestions = new ArrayList<>();
                        try {
                            JSONArray suggestionGroupsA = (JSONArray) response.get("suggestionGroups");
                            JSONObject suggestionGroups = (JSONObject) suggestionGroupsA.get(0);
                            JSONArray searchSuggestions = (JSONArray) suggestionGroups.get("searchSuggestions");
                            for(int i=0; i<searchSuggestions.length(); i++){
                                JSONObject suggestionObj = (JSONObject) searchSuggestions.get(i);
                                String suggestion = (String) suggestionObj.get("displayText");
                                retrievedSuggestions.add(suggestion);
                            }
                            suggestions = retrievedSuggestions;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println(suggestions.toString());
                        callback.onSuccess();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("LNBECKER error: " + error);
                    }
                }) {
            /** Passing some request headers* */
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("Content-Type", "application/json");
                headers.put("Ocp-Apim-Subscription-Key", "77b377c7f136460c826a010ae6eed3cc");
                return headers;
            }
        };

        MyContentProvider.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location permission")
                        .setMessage("Location message")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // allowed permission
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }

                } else {
                    //permission denied uh oh

                }
                return;
            }

        }
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
            getWeather(new VolleyBlankCallback(){
                @Override
                public void onSuccess() {
                    updateViews();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void updateViews(){
        weatherCard.setVisibility(View.VISIBLE);
        ImageView weatherImageView = findViewById(R.id.weatherImage);
        TextView cityView = findViewById(R.id.cityView);
        TextView stateView = findViewById(R.id.stateView);
        TextView tempView = findViewById(R.id.tempView);
        TextView descView = findViewById(R.id.descView);

        cityView.setText(city);
        stateView.setText(state);
        tempView.setText(temp);
        descView.setText(weatherDescription);
        switch (weatherDescription){
            case "Clear":
                Glide.with(this).load("https://csci571.com/hw/hw9/images/android/clear_weather.jpg").into(weatherImageView);
                break;
            case "Clouds":
                Glide.with(this).load("https://csci571.com/hw/hw9/images/android/cloudy_weather.jpg").into(weatherImageView);
                break;
            case "Snow":
                Glide.with(this).load("https://csci571.com/hw/hw9/images/android/snowy_weather.jpeg").into(weatherImageView);
                break;
            case "Rain":
                Glide.with(this).load("https://csci571.com/hw/hw9/images/android/rainy_weather.jpg").into(weatherImageView);
                break;
            case "Drizzle":
                Glide.with(this).load("https://csci571.com/hw/hw9/images/android/rainy_weather.jpg").into(weatherImageView);
                break;
            case "Thunderstorm":
                Glide.with(this).load("https://csci571.com/hw/hw9/images/android/thunder_weather.jpg").into(weatherImageView);
                break;
            default:
                Glide.with(this).load("https://csci571.com/hw/hw9/images/android/sunny_weather.jpg").into(weatherImageView);
        }
    }

    public void getWeather(final VolleyBlankCallback callback){
        String api_key = "7e026d6849e83ec3f6d8bccf98b81cf7";
        String cityEncoded = city.replaceAll("\\s+", "%20");
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityEncoded + "&units=metric&appid=" + api_key;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray weather = response.getJSONArray("weather");
                            JSONObject wObj = (JSONObject) weather.get(0);
                            weatherDescription = (String) wObj.get("main");

                            JSONObject mainObj = response.getJSONObject("main");
                            String tempS = mainObj.get("temp").toString();
                            if (tempS.contains(".")){
                                //temp is double
                                Double tempD = (Double) mainObj.get("temp");
                                temp = (int)Math.round(tempD) + " °C";
                            }
                            else {
                                //temp is int
                                temp = ((int) mainObj.get("temp")) + " °C";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callback.onSuccess();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                    }
                });

        // Access the RequestQueue through your singleton class.
        MyContentProvider.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

}
