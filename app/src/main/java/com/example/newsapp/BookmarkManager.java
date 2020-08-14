package com.example.newsapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class BookmarkManager {
    Context context;

    public BookmarkManager(Context context){
        this.context = context;
    }
    public void saveArticle(NewsCard newsCard){
        //Get current favorites
        ArrayList<NewsCard> bookmarksList = loadFavorites();

        if(bookmarksList == null){
            bookmarksList = new ArrayList<>();
            bookmarksList.add(newsCard);
        }
        else {
            //add to current list
            bookmarksList.add(newsCard);
        }
        saveFavoritesList(bookmarksList);
    }

    public void saveFavoritesList(ArrayList<NewsCard> bookmarksList){
        //Save favorites to shared preferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bookmarksList);
        editor.putString("myBookmarks", json);
        editor.apply();
    }

    public void clearAllBookmarks(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
    }

    public ArrayList<NewsCard> loadFavorites(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("myBookmarks", null);
        Type type = new TypeToken<ArrayList<NewsCard>>() {}.getType();
        ArrayList<NewsCard> bookmarksList = gson.fromJson(json, type);
        return bookmarksList;
    }

//    public ArrayList<String> loadBookmarkIds() {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
//        Gson gson = new Gson();
//        String json = sharedPreferences.getString("myBookmarksIds", null);
//        Type type = new TypeToken<ArrayList<String>>() {}.getType();
//        ArrayList<String> ids = gson.<ArrayList<String>>fromJson(json, type);
//        return ids;
//    }

    public void removeArticle(NewsCard newsCard){
        ArrayList<NewsCard> bookmarksList = loadFavorites();
        if (bookmarksList != null){
            if (bookmarksList.contains(newsCard)){
                bookmarksList.remove(newsCard);
                //Save new list
                saveFavoritesList(bookmarksList);
            }
        }
    }
}
