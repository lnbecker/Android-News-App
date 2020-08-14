package com.example.newsapp;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements NewsCardAdapter.OnCardListener {
    private RecyclerView.Adapter newsCardAdapter;
    private RecyclerView newsRecyclerView;
    private LinearLayoutManager lm;
    private List<NewsCard> newsCards;
    private BookmarkManager bookmarkManager;
    private RelativeLayout progressLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String query;
    private RelativeLayout noResultsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //setting toolbar
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noResultsLayout = findViewById(R.id.noResultsLayoutSearch);
        noResultsLayout.setVisibility(View.GONE);

        newsRecyclerView = (RecyclerView) findViewById(R.id.news_recycler_view);
        bookmarkManager = new BookmarkManager(this);

        progressLayout = findViewById(R.id.progressLayoutSearch);
        progressLayout.setVisibility(View.VISIBLE);

        swipeRefreshLayout = findViewById(R.id.searchSwipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    refreshView();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            toolbar.setTitle("Search results for " + query);
            try {
                getSearchResults(query, new VolleyNewsCallback(){
                    @Override
                    public void onSuccess(List<NewsCard> result) {
                        progressLayout.setVisibility(View.GONE);
                        setUpViews();
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

    private void refreshView() throws UnsupportedEncodingException {
        progressLayout.setVisibility(View.VISIBLE);

        getSearchResults(query, new VolleyNewsCallback(){
            @Override
            public void onSuccess(List<NewsCard> result) {
                swipeRefreshLayout.setRefreshing(false);
                progressLayout.setVisibility(View.GONE);
                setUpViews();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void setUpViews(){
        lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        newsRecyclerView.setLayoutManager(lm);
        newsCardAdapter = new NewsCardAdapter(newsCards, this, this);
        newsRecyclerView.setAdapter(newsCardAdapter);
        if (newsCards == null || newsCards.isEmpty()){
            noResultsLayout.setVisibility(View.VISIBLE);
        }
        else {
            noResultsLayout.setVisibility(View.GONE);
        }
    }

    public void getSearchResults(String searchTerm, final VolleyNewsCallback callback) throws UnsupportedEncodingException {

        String url = "http://hw9server.eba-rnemrspm.us-east-1.elasticbeanstalk.com/api/guardiansearch?keyword=" + URLEncoder.encode(searchTerm, "UTF-8");

        JsonArrayRequest jsonArrayRequest;
        jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        List<NewsCard> newsList = new ArrayList<NewsCard>();
                        for(int i=0; i<response.length(); i++){
                            JSONObject article = null;
                            try {
                                article = response.getJSONObject(i);
                                String id = (String) article.get("id");
                                String title = (String) article.get("title");
                                String image = (String) article.get("image");
                                String section = (String) article.get("section");
                                String date = (String) article.get("date");
                                String desc = (String) article.get("desc");
                                String shareUrl = (String) article.get("shareUrl");
                                String source = (String) article.get("source");
                                NewsCard nc = new NewsCard(id, title, image, section, date, desc, shareUrl, source);
                                newsList.add(nc);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        newsCards = newsList;
                        callback.onSuccess(newsList);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

// Access the RequestQueue through your singleton class.
        MyContentProvider.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    @Override
    public void onCardClick(int position){
        Intent intent = new Intent(this, ArticleDetailsActivity.class);
        NewsCard card = newsCards.get(position);
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
        ArrayList<NewsCard> bookmarks = bookmarkManager.loadFavorites();
        if(bookmarks != null){
            System.out.println(bookmarks.toString());
        }

        final NewsCard nc = newsCards.get(position);

        //Dialog stuff
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_article_long_click);

        TextView titleView = (TextView) dialog.findViewById(R.id.titleDialogView);
        ImageView imageView = (ImageView) dialog.findViewById(R.id.imageDialogView);
        ImageButton twitterBtn = (ImageButton) dialog.findViewById(R.id.twitterButton);
        final ImageButton bookmarkBtn = (ImageButton) dialog.findViewById(R.id.bookmarkButton);
        if (bookmarks != null && bookmarks.contains(nc)){
            bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_icon));
        }
        else {
            bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_border));
        }

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
                if(cBookmarks == null || !cBookmarks.contains(nc)){
                    //This article is not bookmarked
                    bookmarkManager.saveArticle(nc);
                    //update bookmark icon
                    bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_icon));
                    toastMessage = " \"" + nc.getTitle() + "\" was added to Bookmarks";

                }
                else {
                    //article is saved
                    bookmarkManager.removeArticle(nc);
                    //update icon
                    bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_border));
                    toastMessage = " \"" + nc.getTitle() + "\" was removed from Bookmarks";
                }

                Toast.makeText(context, toastMessage,
                        Toast.LENGTH_LONG).show();

                newsCardAdapter.notifyDataSetChanged();
            }
        });

        dialog.show();

    }

}
