package com.android.bignerdranch.picgallery.Network;

import android.location.Location;
import android.net.Uri;
import android.util.JsonReader;
import android.util.Log;

import com.android.bignerdranch.picgallery.Model.PhotoModel;
import com.android.bignerdranch.picgallery.Model.PhotoRequestResult;
import com.android.bignerdranch.picgallery.Model.PhotoResults;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Kaustav on 02-12-2017.
 */

public class FlickrFetcher {

    public static final String API_KEY = "6022ca47887cb83c84b2b3c4a615de19";
    public static final String METHOD_RECENTS = "flickr.photos.getRecent";
    public static final String METHOD_SEARCH = "flickr.photos.search";

    public static final String TAG = "JSONexception";
    private FlickerService flickerClient = null;
    private Map<String,String> querymap = new HashMap<>();

    public FlickrFetcher(){
        if(flickerClient==null){
            flickerClient = new Retrofit.Builder()
                    .baseUrl(FlickerService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(FlickerService.class);
        }
        querymap.put("api_key",API_KEY);
        querymap.put("format", "json");
        querymap.put("nojsoncallback", "1");
        querymap.put("extras","url_s");
    }


    public byte[] getBytes(String url) throws IOException {
        URL url1 = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {

            InputStream inputStream = urlConnection.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while((bytesRead = inputStream.read(buffer)) > 0){
                outputStream.write(buffer,0,bytesRead);
            }
        }
        finally {
            urlConnection.disconnect();
        }
        outputStream.close();
        return outputStream.toByteArray();
    }

    public String getString(String url) throws IOException{
        return new String(getBytes(url));
    }

    public PhotoRequestResult fetchItems(String url) throws IOException {

        List<PhotoModel> photoModels = new ArrayList<>();
        PhotoRequestResult photoRequestResult = null;

        try {
            String jsonOutput = getString(url);
            Gson gson = new Gson();
            photoRequestResult = gson.fromJson(jsonOutput,PhotoRequestResult.class);
            //photoModels = photoRequestResult.getResults();
           // parseItems(photoModels, jsonOutput);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return photoRequestResult;
    }

    public String buildUrl(String method,String query,int page){
         Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
                .buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras","url_s")
                .build();
        Uri.Builder finalUri = ENDPOINT.buildUpon().appendQueryParameter("method",method);

        if(method.equals(METHOD_SEARCH)){
            finalUri.appendQueryParameter("text",query);
        }

        if(page != -1)
            finalUri.appendQueryParameter("page",Integer.toString(page));

        return finalUri.toString();
    }

    private void prepareForRetrofit(String method,String query,int page){
        querymap.put("method",method);

        if(method.equals(METHOD_SEARCH)){
            if(query!=null)
                querymap.put("text",query);
        }

        if(page != -1)
            querymap.put("page",Integer.toString(page));

    }

    public PhotoRequestResult getSearchItems(int page,String query) throws IOException {

        return fetchItems(buildUrl(METHOD_SEARCH,query,page));
    }

    public PhotoRequestResult getRecentItems(int page) throws IOException {

        return fetchItems(buildUrl(METHOD_RECENTS,null,page));
    }

    public Call<PhotoRequestResult> getRecentItemsFromRetrofit(int page) throws IOException{
        prepareForRetrofit(METHOD_RECENTS,null,page);
        return flickerClient.getPublicPics(querymap);
    }

    public Call<PhotoRequestResult> getSearchItemsFromRetrofit(int page,String query) throws IOException{
        prepareForRetrofit(METHOD_SEARCH,query,page);
        return flickerClient.getPublicPics(querymap);
    }

    public Call<PhotoRequestResult> getGeoSearchItemsFromRetrofit(int page, String query, Location location) throws IOException{
        prepareForRetrofit(METHOD_SEARCH,query,page);
        querymap.put("lat",String.valueOf(location.getLatitude()));
        querymap.put("lon",String.valueOf(location.getLongitude()));
        return flickerClient.getPublicPics(querymap);
    }

    private void parseItems(List<PhotoModel> photoModels, String jsonOutput) throws JSONException {
        JSONObject photos = new JSONObject(jsonOutput);
        JSONObject photosObject = photos.getJSONObject("photos");
        JSONArray photoArray = photosObject.getJSONArray("photo");

        for(int i = 0;i<photoArray.length();i++){
            JSONObject photoObject = photoArray.getJSONObject(i);
            PhotoModel photoModel = new PhotoModel();
            photoModel.setId(photoObject.getString("id"));
            photoModel.setTitle(photoObject.getString("title"));
            if(!photoObject.has("url_s"))
                continue;
            photoModel.setUrl(photoObject.getString("url_s"));
            photoModels.add(photoModel);
        }
    }
}
