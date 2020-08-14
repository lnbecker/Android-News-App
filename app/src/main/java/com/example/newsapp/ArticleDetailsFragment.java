package com.example.newsapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;


public class ArticleDetailsFragment extends Fragment {
    private View articalDetailsView = null;

    public void showNews(String id, String title, String image, String section, String date, String desc, String url)
    {
        TextView titleTextView = (TextView)articalDetailsView.findViewById(R.id.articleTitle);
        TextView dateTextView = (TextView)articalDetailsView.findViewById(R.id.articleDate);
        TextView descTextView = (TextView)articalDetailsView.findViewById(R.id.articleDesc);
        TextView sectionTextView = (TextView)articalDetailsView.findViewById(R.id.articleSection);
        TextView urlTextView = (TextView)articalDetailsView.findViewById(R.id.articleUrl);
        ImageView imageView = (ImageView)articalDetailsView.findViewById(R.id.articleImage) ;

        titleTextView.setText(title);
        dateTextView.setText(date);
        descTextView.setText(desc);
        Spanned linkText = Html.fromHtml("<a href='" + url + "'>View Full Article</a>");
        urlTextView.setMovementMethod(LinkMovementMethod.getInstance());
        urlTextView.setText(linkText);
        sectionTextView.setText(section);
        Glide.with(getContext()).load(image).into(imageView);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_article_details, container, false);
        this.articalDetailsView = view;
        return view;
    }
}
