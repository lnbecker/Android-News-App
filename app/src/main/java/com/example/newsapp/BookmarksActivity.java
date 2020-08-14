package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarksActivity extends AppCompatActivity  implements BookmarkedNewsCardAdapter.OnCardListener, RemovedBookmarksListener {
    private RecyclerView.Adapter bookmarksAdapter;
    private RecyclerView bookmarksRecyclerView;
    private GridLayoutManager lm;
    private List<NewsCard> bookmarkCards;
    private BookmarkManager bookmarkManager;
    private Toolbar toolbar;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> suggestionsAdapter;
    private String query;
    ArrayList<String> suggestions;
    private SearchView searchView;
    private RelativeLayout emptyBookmarksLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        //setting toolbar
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        bookmarksRecyclerView = (RecyclerView) findViewById(R.id.bookmarksRecyclerView);
        bookmarkManager = new BookmarkManager(this);
        bookmarkCards = bookmarkManager.loadFavorites();

        emptyBookmarksLayout = findViewById(R.id.emptyBookmarksLayout);
        if (bookmarkCards == null || bookmarkCards.isEmpty()){
            emptyBookmarksLayout.setVisibility(View.VISIBLE);
        }
        else {
            emptyBookmarksLayout.setVisibility(View.GONE);
        }


        //Navigation menu listener
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Intent home = new Intent(BookmarksActivity.this,MainActivity.class);
                        startActivity(home);
                        break;
                    case R.id.action_headlines:
                        Intent headlines = new Intent(BookmarksActivity.this,TopHeadlinesActivity.class);
                        startActivity(headlines);
                        break;
                    case R.id.action_trending:
                        Intent trending = new Intent(BookmarksActivity.this,TrendingActivity.class);
                        startActivity(trending);
                        break;
                    case R.id.action_bookmarks:
                        break;
                }
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_bookmarks);
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
        lm = new GridLayoutManager(this, 2);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        bookmarksRecyclerView.setLayoutManager(lm);
        bookmarksAdapter = new BookmarkedNewsCardAdapter(bookmarkCards, this, this, this);
        bookmarksRecyclerView.setAdapter(bookmarksAdapter);

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

    @Override
    public void onCardClick(int position){
        Intent intent = new Intent(this, ArticleDetailsActivity.class);
        NewsCard card = bookmarkCards.get(position);
        intent.putExtra("ARTICLE_ID", card.getId());
        intent.putExtra("ARTICLE_TITLE", card.getTitle());
        intent.putExtra("ARTICLE_IMAGE", card.getImage());
        intent.putExtra("ARTICLE_DESC", card.getDesc());
        intent.putExtra("ARTICLE_SECTION", card.getSection());
        intent.putExtra("ARTICLE_URL", card.getShareUrl());
        intent.putExtra("ARTICLE_DATE", card.getPublishedDate());
        startActivity(intent);
    }

    @Override
    public void onCardLongClick(final int position){
        final Context context = this;
        final NewsCard nc = bookmarkCards.get(position);

        //Dialog stuff
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_article_long_click);

        TextView titleView = (TextView) dialog.findViewById(R.id.titleDialogView);
        ImageView imageView = (ImageView) dialog.findViewById(R.id.imageDialogView);
        ImageButton twitterBtn = (ImageButton) dialog.findViewById(R.id.twitterButton);
        final ImageButton bookmarkBtn = (ImageButton) dialog.findViewById(R.id.bookmarkButton);
        bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_icon));

        titleView.setText(nc.getTitle());
        Glide.with(this).load(nc.getImage()).into(imageView);
        twitterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweetUrl = "https://twitter.com/intent/tweet?text=Check out this link &url="
                        + nc.getShareUrl();
                Uri uri = Uri.parse(tweetUrl);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Reload bookmarks to check
                ArrayList<NewsCard> cBookmarks = bookmarkManager.loadFavorites();
                String toastMessage;
                //article is removed
                bookmarkManager.removeArticle(nc);
                //update icon
                bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_border));
                toastMessage = " \"" + nc.getTitle() + "\" was removed from Bookmarks";
                //bookmarksAdapter.notifyItemRemoved(position);
                bookmarkCards.remove(nc);
                bookmarksAdapter.notifyDataSetChanged();
                dialog.dismiss();;

                if (bookmarkCards == null || bookmarkCards.isEmpty()){
                    emptyBookmarksLayout.setVisibility(View.VISIBLE);
                    emptyBookmarksLayout.invalidate();
                }

                Toast.makeText(context, toastMessage,
                        Toast.LENGTH_LONG).show();

            }
        });

        dialog.show();

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

    @Override
    public void onRemovedBookmarks() {
        emptyBookmarksLayout.setVisibility(View.VISIBLE);
    }
}
