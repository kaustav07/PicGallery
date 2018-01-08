package com.android.bignerdranch.picgallery.Network;

import android.net.Uri;

import com.android.bignerdranch.picgallery.Model.PhotoRequestResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.InstanceOf;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;

/**
 * Created by Kaustav on 05-01-2018.
 */

public class FlickrFetcherTest {


    FlickrFetcher fetcher;
    Call<PhotoRequestResult> mResult;
    PhotoRequestResult mPhotoRequestResult;
    public static final String url = "http://www.facebook.com";
    CountDownLatch mLatch;

    @Before
    public void setup() throws IOException {
        fetcher = new FlickrFetcher();
        mLatch = new CountDownLatch(1);
        mResult = null;
        mPhotoRequestResult = null;
    }

    @Test
    public void getBytes() throws Exception {
        assertNotNull(fetcher.getBytes(url));
    }

    @Test
    public void getString() throws Exception {
        assertEquals("this is a fail",anyString(),fetcher.getString(url));
    }

    @Test
    public void fetchItems() throws Exception {
        mResult = fetcher.getRecentItemsFromRetrofit(1);
        assertNotNull("PhotoRequest not fetched",mResult);
        assertThat("not Call type",mResult, instanceOf(Call.class));
        mResult.enqueue(new Callback<PhotoRequestResult>() {
            @Override
            public void onResponse(Call<PhotoRequestResult> call, Response<PhotoRequestResult> response) {
                mPhotoRequestResult = response.body();
                mLatch.countDown();
            }

            @Override
            public void onFailure(Call<PhotoRequestResult> call, Throwable t) {
                mPhotoRequestResult = null;
                mLatch.countDown();
            }
        });

        mLatch.await();

        assertNotNull("PhotoRequest is null",mPhotoRequestResult);
        assertThat("not PhotoRequest Type",mPhotoRequestResult,instanceOf(PhotoRequestResult.class));
    }

    @Test
    public void getSearchItems() throws Exception {
        mResult = null;
        mResult = fetcher.getSearchItemsFromRetrofit(1,anyString());
        assertNotNull(mResult);
        assertThat("not Call type",mResult, instanceOf(Call.class));
        mResult.enqueue(new Callback<PhotoRequestResult>() {
            @Override
            public void onResponse(Call<PhotoRequestResult> call, Response<PhotoRequestResult> response) {
                mPhotoRequestResult = response.body();
                mLatch.countDown();
            }

            @Override
            public void onFailure(Call<PhotoRequestResult> call, Throwable t) {
                mPhotoRequestResult = null;
                mLatch.countDown();
            }
        });

        mLatch.await();

        assertNotNull("PhotoRequest is null",mPhotoRequestResult);
        assertThat("not PhotoRequest Type",mPhotoRequestResult,instanceOf(PhotoRequestResult.class));
    }

}