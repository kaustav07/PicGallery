package com.android.bignerdranch.picgallery.Network;

import com.android.bignerdranch.picgallery.Model.PhotoRequestResult;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by Kaustav on 15-12-2017.
 */

public interface FlickerService {

    public static final String BASE_URL = "https://api.flickr.com/";

    @GET("services/rest/")
    Call<PhotoRequestResult> getPublicPics(@QueryMap Map<String, String> options);
}
