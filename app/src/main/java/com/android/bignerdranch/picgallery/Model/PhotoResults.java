package com.android.bignerdranch.picgallery.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Kaustav on 05-12-2017.
 */

public class PhotoResults {
    int page;
    int pages;
    int perpage;
    int total;
    @SerializedName("photo")
    List<PhotoModel> photolist;

    List<PhotoModel> getPhotolist() {
        return photolist;
    }
    int getItemsPerPage() {
        return perpage;
    }
    int getMaxPages() {
        return pages;
    }
    int getTotal() {
        return total;
    }
}
