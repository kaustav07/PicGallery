package com.android.bignerdranch.picgallery.Model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Kaustav on 05-01-2018.
 */
public class PhotoRequestResultTest {
    PhotoRequestResult mResult;
    PhotoResults mPhotoResults;

    @Before
    public void setup() {
        mResult = new PhotoRequestResult();
        mResult.photos = new PhotoResults();
    }

    @Test
    public void getResults() throws Exception {
        assertEquals(mResult.getResults(),mResult.photos.getPhotolist());
    }

    @Test
    public void getPageCount() throws Exception {
        assertEquals(mResult.getPageCount(),mResult.photos.getMaxPages());
    }

    @Test
    public void getItemCount() throws Exception {
        assertEquals(mResult.getItemCount(),mResult.photos.getTotal());
    }

    @Test
    public void getItemsPerPage() throws Exception {
        assertEquals(mResult.getItemsPerPage(),mResult.photos.getItemsPerPage());
    }

}