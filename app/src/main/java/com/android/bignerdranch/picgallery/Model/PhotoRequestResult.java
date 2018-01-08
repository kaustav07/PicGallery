package com.android.bignerdranch.picgallery.Model;

import java.util.List;

/**
 * Created by Kaustav on 05-12-2017.
 */

public class PhotoRequestResult {
    PhotoResults photos;
    String stat;

    public  List<PhotoModel> getResults() {
        return photos.getPhotolist();
    }
    public int getPageCount() {
        return photos.getMaxPages();
    }
    public int getItemCount() {
        return photos.getTotal();
    }
    public int getItemsPerPage() {
        return photos.getItemsPerPage();
    }
}
