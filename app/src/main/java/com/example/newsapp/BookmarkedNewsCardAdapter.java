package com.example.newsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class BookmarkedNewsCardAdapter extends RecyclerView.Adapter<BookmarkedNewsCardAdapter.BookmarkedNewsCardViewHolder>{
    private List<NewsCard> newsList;
    private OnCardListener onCardListener;
    private Context context;
    public RemovedBookmarksListener removedBookmarksListener;;


    public BookmarkedNewsCardAdapter(List<NewsCard> newsList, OnCardListener onCardListener, RemovedBookmarksListener removedBookmarksListener, Context context) {
        this.newsList = newsList;
        this.onCardListener = onCardListener;
        this.removedBookmarksListener = removedBookmarksListener;
        this.context = context;
    }

    @Override
    public BookmarkedNewsCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_news_card_bookmarked_item, parent, false);
        return new BookmarkedNewsCardViewHolder(view, onCardListener);




    }

    @Override
    public void onBindViewHolder(final BookmarkedNewsCardViewHolder holder, final int position) {
        final NewsCard newsCard = newsList.get(position);
        holder.titleView.setText(newsCard.getTitle());
        Glide.with(context).load(newsCard.getImage()).into(holder.imageView);
        holder.timePassedView.setText(newsCard.getPublishedDate());
        holder.sectionView.setText(newsCard.getSection());

        final BookmarkManager bookmarkManager = new BookmarkManager(context);
        ArrayList<NewsCard> bookmarks = bookmarkManager.loadFavorites();
        holder.bookmarkIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_icon));


        //Click function for bookmark button
        holder.bookmarkIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Reload bookmarks to check
                ArrayList<NewsCard> cBookmarks = bookmarkManager.loadFavorites();
                String toastMessage;
                //article is removed
                bookmarkManager.removeArticle(newsCard);
                toastMessage = " \"" + newsCard.getTitle() + "\" was removed from Bookmarks";

                Toast.makeText(context, toastMessage,
                        Toast.LENGTH_LONG).show();

                newsList.remove(newsCard);
                notifyDataSetChanged();
                if (newsList== null || newsList.isEmpty()){
                    removedBookmarksListener.onRemovedBookmarks();
                }

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

    public class BookmarkedNewsCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView titleView;
        public ImageView imageView;
        public TextView timePassedView;
        public TextView sectionView;
        public CardView cardView;
        public OnCardListener onCardListener;
        public ImageView bookmarkIcon;


        public BookmarkedNewsCardViewHolder(View itemView, OnCardListener onCardListener) {
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
