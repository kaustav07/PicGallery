package com.android.bignerdranch.picgallery.Model;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kaustav on 02-12-2017.
 */

public class PhotoModel {

    @SerializedName("id")
    String mId;
    @SerializedName("title")
    String mTitle;
    @SerializedName("url_s")
    String mUrl;
    @SerializedName("owner")
    String mOwner;

    @Override
    public String toString(){
        return mTitle;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    public Uri getPicPageUri(){
        return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }
}
