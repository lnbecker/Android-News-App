package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SearchView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrendingActivity extends AppCompatActivity implements View.OnKeyListener {

    private String searchTerm;
    private List<Entry> entryList;
    private LineChart chart;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> suggestionsAdapter;
    private String query;
    ArrayList<String> suggestions;
    private SearchView searchView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        //setting toolbar
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);


        //Search term stuff
        EditText searchInput = (EditText) findViewById(R.id.textInput);
        searchInput.setOnKeyListener(this);

        //Chart initializiation
        chart = findViewById(R.id.lineChart);
        Legend legend = chart.getLegend();
        legend.setTextColor(Color.parseColor("black"));
        legend.setTextSize(14);
        chart.setDrawGridBackground(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        searchTerm = "CoronaVirus";
        try {
            getTrend(new VolleyBlankCallback(){
                @Override
                public void onSuccess() {
                    populateChart();
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //Navigation menu listener
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Intent home = new Intent(TrendingActivity.this,MainActivity.class);
                        startActivity(home);
                        break;
                    case R.id.action_headlines:
                        Intent headlines = new Intent(TrendingActivity.this,TopHeadlinesActivity.class);
                        startActivity(headlines);
                        break;
                    case R.id.action_trending:
                        break;
                    case R.id.action_bookmarks:
                        Intent bookmarks = new Intent(TrendingActivity.this,BookmarksActivity.class);
                        startActivity(bookmarks);
                        break;
                }
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_trending);
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {

        //Search term stuff
        EditText searchInput = (EditText) findViewById(R.id.textInput);
        searchTerm = searchInput.getText().toString();

        if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

            if (!event.isShiftPressed()) {

                //call google trends api
                try {
                    getTrend(new VolleyBlankCallback(){
                        @Override
                        public void onSuccess() {
                            populateChart();
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return true;
            }

        }
        return false; // pass on to other listeners.

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
//                            suggestionsAdapter.notifyDataSetChanged();
//                            autoCompleteTextView.setAdapter(suggestionsAdapter);
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

    public void populateChart(){

        //Create data set
        String label = "Trending chart for " + searchTerm;
        LineDataSet dataSet = new LineDataSet(entryList, label);
        dataSet.setCircleColor(ColorTemplate.rgb("#ae95e0"));
        dataSet.setColor(ColorTemplate.rgb("#ae95e0"));
        dataSet.setDrawCircleHole(false);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        LineData data = new LineData(dataSet);
        chart.setData(data);
        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    public void getTrend(final VolleyBlankCallback callback) throws UnsupportedEncodingException {
        String url = "http://hw9server.eba-rnemrspm.us-east-1.elasticbeanstalk.com/api/trends?term=" + URLEncoder.encode(searchTerm, "UTF-8");

        JsonArrayRequest jsonArrayRequest;
        jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        entryList = new ArrayList<Entry>();
                        for(int i=0; i<response.length(); i++){
                            try {
                                int value = (int) response.get(i);
                                entryList.add(new Entry(i, value));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
        MyContentProvider.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
}
