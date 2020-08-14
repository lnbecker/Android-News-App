package com.example.newsapp;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class NewsCard {
    public String articleId;
    public String title;
    public String publishedDate;
    public String timeAgo;
    public String image;
    public String shareUrl;
    public String desc;
    public String section;
    public String source;
    public String dateFormatted;

    public NewsCard(String articleId, String title, String image, String section, String publishedDate, String desc, String shareUrl, String source){
        this.articleId = articleId;
        this.title = title;
        this.image = image;
        this.section = section.substring(0,1) + section.substring(1).toLowerCase();
        this.publishedDate = publishedDate;
        this.desc = desc;
        this.shareUrl = shareUrl;
        this.source = source;

        calculateTimeAgo();
        formatDate();
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getDesc(){
        return this.desc;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public String getImage(){
        return this.image;
    }

    public void setImage(String image){
        this.image = image;
    }

    public String getShareUrl(){
        return this.shareUrl;
    }

    public void setShareUrl(String url){
        this.shareUrl = url;
    }

    public String getSection(){
        return this.section;
    }

    public void setSection(String section){
        this.section = section;
    }

    public String getSource(){
        return this.source;
    }

    public void setSource(String source){
        this.source = source;
    }

    public String getId(){
        return this.articleId;
    }

    public void setId(String articleId){
        this.articleId = articleId;
    }

    public String getPublishedDate(){
        return this.publishedDate;
    }

    public void setPublishedDate(String date){
        this.publishedDate = date;
    }

    public String getTimeAgo(){
        return this.timeAgo;
    }

    public void setTimeAgo(String timeAgo){
        this.timeAgo = timeAgo;
    }

    public String getDateFormatted(){
        return this.dateFormatted;
    }

    public void setDateFormatted(String dateFormatted){
        this.dateFormatted = dateFormatted;
    }

    @Override
    public boolean equals(Object other){
        if (this.getClass() == other.getClass()){
            NewsCard otherNewsCard = (NewsCard) other;
            if(this.getId().equals(otherNewsCard.getId()))
            {
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }

    private void calculateTimeAgo(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ZonedDateTime datePublished = ZonedDateTime.parse(publishedDate);
            ZoneId toTimeZone = ZoneId.of("America/Los_Angeles");
            datePublished = datePublished.withZoneSameInstant(toTimeZone);
            Clock cl = Clock.system(toTimeZone);
            ZonedDateTime dateToday = ZonedDateTime.now(cl);
            Long difference = datePublished.until(dateToday, ChronoUnit.SECONDS);
            if (difference > 60){
                //check for minutes or hours
                difference = datePublished.until(dateToday, ChronoUnit.MINUTES);
                if(difference > 60){
                    //hours ago
                    difference = datePublished.until(dateToday, ChronoUnit.HOURS);
                    if (difference > 24){
                        //days ago
                        difference = datePublished.until(dateToday, ChronoUnit.DAYS);
                        timeAgo = difference + "d ago";
                    }
                    else {
                        //hours
                        timeAgo = difference + "h ago";

                    }
                }
                else {
                    //minutes ago
                    timeAgo = difference + "m ago";
                }
            }
            else {
                //posted seconds ago
                timeAgo = difference + "s ago";
            }
        }
        else {
            timeAgo = publishedDate;
        }
        timeAgo = timeAgo;
    }

    private void formatDate(){
        String DATE_FORMAT = "dd MMM yyyy";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(publishedDate);
       dateFormatted = formatter.format(zonedDateTime);
    }

}
