package com.example.newsapp;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class TopHeadlinesActivity extends AppCompatActivity {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    PagerAdapter pagerAdapter;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> suggestionsAdapter;
    private String query;
    ArrayList<String> suggestions;
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_headlines);

        //TAB STUFF
        //link views
        toolbar = findViewById(R.id.toolBar);
        tabLayout = findViewById(R.id.mTabLayout);
        viewPager = findViewById(R.id.viewPager);

        //initializing toolbar
        setSupportActionBar(toolbar);

        //adapter setup
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        //attaching fragments to adapter
        pagerAdapter.addFragment(new WorldFragment(),"World");
        pagerAdapter.addFragment(new BusinessFragment(),"Business");
        pagerAdapter.addFragment(new PoliticsFragment(),"Politics");
        pagerAdapter.addFragment(new SportsFragment(),"Sports");
        pagerAdapter.addFragment(new TechnologyFragment(),"Technology");
        pagerAdapter.addFragment(new ScienceFragment(),"Science");

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


        //Navigation menu listener
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Intent home = new Intent(TopHeadlinesActivity.this,MainActivity.class);
                        startActivity(home);
                        break;
                    case R.id.action_headlines:
                        break;
                    case R.id.action_trending:
                        Intent trending = new Intent(TopHeadlinesActivity.this,TrendingActivity.class);
                        startActivity(trending);
                        break;
                    case R.id.action_bookmarks:
                        Intent bookmarks = new Intent(TopHeadlinesActivity.this,BookmarksActivity.class);
                        startActivity(bookmarks);
                        break;
                }
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_headlines);
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

    private void setUpViews(){
        //Search stuff
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

}
