package com.example.newsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class NewsCardAdapter extends RecyclerView.Adapter<NewsCardAdapter.NewsCardViewHolder>{
    private List<NewsCard> newsList;
    private OnCardListener onCardListener;
    private Context context;


    public NewsCardAdapter(List<NewsCard> newsList, OnCardListener onCardListener, Context context) {
        this.newsList = newsList;
        this.onCardListener = onCardListener;
        this.context = context;
    }

    public void updateItem(int position){
        System.out.println("hello");
    }

    @Override
    public NewsCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_news_card_item, parent, false);
        return new NewsCardViewHolder(view, onCardListener);
    }

    @Override
    public void onBindViewHolder(final NewsCardViewHolder holder, final int position) {
        final NewsCard newsCard = newsList.get(position);
        holder.titleView.setText(newsCard.getTitle());
        Glide.with(context).load(newsCard.getImage()).into(holder.imageView);
        holder.timePassedView.setText(newsCard.getTimeAgo());
        holder.sectionView.setText(newsCard.getSection());

        final BookmarkManager bookmarkManager = new BookmarkManager(context);
        ArrayList<NewsCard> bookmarks = bookmarkManager.loadFavorites();
        if (bookmarks != null && bookmarks.contains(newsCard)){
            holder.bookmarkIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_icon));
        }
        else {
            holder.bookmarkIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_border));
        }

        //Click function for bookmark button
        holder.bookmarkIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Reload bookmarks to check
                ArrayList<NewsCard> cBookmarks = bookmarkManager.loadFavorites();
                String toastMessage;
                if(cBookmarks == null || !cBookmarks.contains(newsCard)){
                    //This article is not bookmarked
                    bookmarkManager.saveArticle(newsCard);
                    //update bookmark icon
                    //holder.bookmarkIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_icon));
                    toastMessage = " \"" + newsCard.getTitle() + "\" was added to Bookmarks";

                }
                else {
                    //article is saved
                    bookmarkManager.removeArticle(newsCard);
                    //update icon
                    //bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_border));
                    toastMessage = " \"" + newsCard.getTitle() + "\" was removed from Bookmarks";
                }

                Toast.makeText(context, toastMessage,
                        Toast.LENGTH_LONG).show();


                notifyItemChanged(position);

            }
        });
    }

    @Override
    public int getItemCount() {
        int ret = 0;
        if(newsList!=null)
        {
            ret = newsList.size();
        }
        return ret;
    }

    public class NewsCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView titleView;
        public ImageView imageView;
        public TextView timePassedView;
        public TextView sectionView;
        public CardView cardView;
        public OnCardListener onCardListener;
        public ImageView bookmarkIcon;


        public NewsCardViewHolder(View itemView, OnCardListener onCardListener) {
            super(itemView);
            this.onCardListener = onCardListener;

            titleView = itemView.findViewById(R.id.title);
            imageView = itemView.findViewById(R.id.image);
            timePassedView = itemView.findViewById(R.id.timePassed);
            sectionView = itemView.findViewById(R.id.section);
            cardView = itemView.findViewById(R.id.card_view);
            bookmarkIcon = itemView.findViewById(R.id.bookmarkIcon);


            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        // Handles the row being being clicked
        @Override
        public void onClick(View view) {
            onCardListener.onCardClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            onCardListener.onCardLongClick(getAdapterPosition());
            return true;
        }

    }

    public interface OnCardListener{
        void onCardClick(int position);
        void onCardLongClick(int position);
    }


}
