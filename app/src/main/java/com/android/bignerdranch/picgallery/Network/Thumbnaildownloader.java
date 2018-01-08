package com.android.bignerdranch.picgallery.Network;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Kaustav on 06-12-2017.
 */

public class Thumbnaildownloader<T> extends HandlerThread {


    private Handler mRequestHandler,mResponseHandler;
    private ConcurrentHashMap<T, String> mConcurrentHashMap = new ConcurrentHashMap<>();
    private ThumbnailDownloadListener<T> mDownloadListener;
    private Boolean mHasquit = false;
    private static final int MESSAGE_DOWNLOAD = 101;

    public Thumbnaildownloader(String name, Handler responseHandler) {
        super(name);
        mResponseHandler = responseHandler;
    }

    @Override
    public boolean quit() {
        mHasquit = true;
        return super.quit();
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD){
                    T target = (T) msg.obj;
                    handleResult(target);
                }
            }
        };
    }

    public interface ThumbnailDownloadListener<T> {
         void onThumbnailDownloaded(T target,Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener){
        mDownloadListener = listener;
    }

    public void queueThumbnail(T target, String url){
        if(url == null){
            mConcurrentHashMap.remove(target);
        }
        else {
            mConcurrentHashMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();
        }
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mConcurrentHashMap.clear();
    }

    private void handleResult(final T target){
        final String url = mConcurrentHashMap.get(target);

        try {
            byte[] bitmapBytes = new FlickrFetcher().getBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mConcurrentHashMap.get(target) != url || mHasquit){
                        return;
                    }
                    else {
                        mConcurrentHashMap.remove(target);
                        mDownloadListener.onThumbnailDownloaded(target,bitmap);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
