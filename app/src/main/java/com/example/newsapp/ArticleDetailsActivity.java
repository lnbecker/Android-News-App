package com.example.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class ArticleDetailsActivity extends AppCompatActivity {
    private final static String INTENT_KEY_NEWS_ID = "ARTICLE_ID";
    private final static String INTENT_KEY_NEWS_TITLE = "ARTICLE_TITLE";
    private final static String INTENT_KEY_NEWS_IMAGE = "ARTICLE_IMAGE";
    private final static String INTENT_KEY_NEWS_DESC = "ARTICLE_DESC";
    private final static String INTENT_KEY_NEWS_SECTION = "ARTICLE_SECTION";
    private final static String INTENT_KEY_NEWS_DATE = "ARTICLE_DATE";
    private final static String INTENT_KEY_NEWS_URL = "ARTICLE_URL";
    private NewsCard newsCard;
    private BookmarkManager bookmarkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_details);

        Intent intent = getIntent();
        String id = intent.getStringExtra(INTENT_KEY_NEWS_ID);
        String title = intent.getStringExtra(INTENT_KEY_NEWS_TITLE);
        String image = intent.getStringExtra(INTENT_KEY_NEWS_IMAGE);
        String desc = intent.getStringExtra(INTENT_KEY_NEWS_DESC);
        String section = intent.getStringExtra(INTENT_KEY_NEWS_SECTION);
        String url = intent.getStringExtra(INTENT_KEY_NEWS_URL);
        String date = intent.getStringExtra(INTENT_KEY_NEWS_DATE);

        newsCard = new NewsCard(id, title, image, section, date, desc, url, "GUARDIAN");
        bookmarkManager = new BookmarkManager(this);

        //App bar
        Toolbar toolbar = findViewById(R.id.toolBar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fragmentManager = getSupportFragmentManager();
        ArticleDetailsFragment fragmentNewsContent = (ArticleDetailsFragment) fragmentManager.findFragmentById(R.id.article_details_fragment);

        fragmentNewsContent.showNews(id, title, image, section, newsCard.getDateFormatted(), desc, url);
    }

    //For adding search feature to app bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_items, menu);

        // Get the SearchView and set the searchable configuration
        final MenuItem bookmarkItem = menu.findItem(R.id.action_bookmark);
        ArrayList<NewsCard> bookmarks = bookmarkManager.loadFavorites();
        if (bookmarks != null && bookmarks.contains(newsCard)){
            bookmarkItem.setIcon(R.drawable.ic_bookmark_icon);
        }
        else {
            bookmarkItem.setIcon(R.drawable.ic_bookmark_border);
        }

        //Icon colors
        Drawable drawable = bookmarkItem.getIcon();
        drawable.mutate();
        drawable.setColorFilter(getResources().getColor(R.color.colorOrange), PorterDuff.Mode.SRC_IN);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_bookmark:
                ArrayList<NewsCard> cBookmarks = bookmarkManager.loadFavorites();
                String toastMessage;
                if(cBookmarks == null || !cBookmarks.contains(newsCard)){
                    //This article is not bookmarked
                    bookmarkManager.saveArticle(newsCard);
                    //update bookmark icon
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_icon));
                    toastMessage = " \"" + newsCard.getTitle() + "\" was added to Bookmarks";

                }
                else {
                    //article is saved
                    bookmarkManager.removeArticle(newsCard);
                    //update icon
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bookmark_border));
                    toastMessage = " \"" + newsCard.getTitle() + "\" was removed from Bookmarks";
                }

                Toast.makeText(this, toastMessage,
                        Toast.LENGTH_LONG).show();

                //Icon colors
                Drawable drawable = item.getIcon();
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.colorOrange), PorterDuff.Mode.SRC_IN);

                invalidateOptionsMenu();
                return true;
            case R.id.action_share:
                String tweetUrl = "https://twitter.com/intent/tweet?text=Check out this link &url="
                        + newsCard.getShareUrl();
                Uri uri = Uri.parse(tweetUrl);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }






}

