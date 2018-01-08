package com.android.bignerdranch.picgallery.Services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.bignerdranch.picgallery.Model.PhotoRequestResult;
import com.android.bignerdranch.picgallery.Network.FlickrFetcher;
import com.android.bignerdranch.picgallery.PicGallery;
import com.android.bignerdranch.picgallery.Preferance.QueryPrefarance;
import com.android.bignerdranch.picgallery.R;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;


public class PollService extends IntentService {

    private static final String TAG = "PollService";
    public static final String ACTION_SHOW_NOTIFICATION = "com.bignerdranch.picgallery.action.SHOW_NOTIFICATION";
    public static final String PRIVATE_NOTIFICATION_PERMISSION = "com.bignerdranch.picgallery.permission.NOTIFICATION_PRIVATE";
    public static final String REQUEST_CODE = "NOTIFICATION_REQUEST_CODE";
    public static final String NOTIFICATION_OBJ = "NOTIFICATION_OBJ";


    public PollService() {
        super("PollService");
    }

    public static Intent newIntent(Context context){
       return new Intent(context,PollService.class);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if(!isNetworkAvailableAndConnected()){
            return;
        }
        try {
            String query = QueryPrefarance.getQueryFromPreference(getApplicationContext());
            String lastresultid = QueryPrefarance.getLastResultId(getApplicationContext());
            PhotoRequestResult requestResult;

            if (query != null)
                requestResult = new FlickrFetcher().getSearchItems(-1, query);
            else
                requestResult = new FlickrFetcher().getRecentItems(-1);

            if(requestResult.getResults().get(0).getId().equals(lastresultid))
                Log.d(TAG,"Got old results");
            else {
                Log.d(TAG, "Got new results");
                PendingIntent pi = PendingIntent.getActivity(this,0, PicGallery.newIntent(this),0);
                Resources resources = getResources();

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel("102", "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

                    // Configure the notification channel.
                    notificationChannel.setDescription("Channel description");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.enableVibration(true);
                    assert notificationManager != null;
                    notificationManager.createNotificationChannel(notificationChannel);

                    builder = new NotificationCompat.Builder(this,"102");
                }
                else {
                    builder = new NotificationCompat.Builder(this);
                }

                Notification notification = builder
                        .setContentTitle(resources.getString(R.string.new_items_title))
                        .setContentText(resources.getString(R.string.new_item_text))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();

                //notificationManagerCompat.notify(0,notification);
                sendBrodcastToNotify(notification);
            }
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }

    }

    private void sendBrodcastToNotify(Notification notification) {
        Intent notifyIntent = new Intent(ACTION_SHOW_NOTIFICATION);
        notifyIntent.putExtra(REQUEST_CODE,0);
        notifyIntent.putExtra(NOTIFICATION_OBJ,notification);
        sendOrderedBroadcast(notifyIntent,PRIVATE_NOTIFICATION_PERMISSION,null,null, Activity.RESULT_OK,null,null);
    }

    public static void setServiceAlarm(Context context,Boolean iOn){

        final long interval = TimeUnit.MINUTES.toMillis(1);
        PendingIntent pi = PendingIntent.getService(context,0,PollService.newIntent(context),0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if(alarmManager != null) {
            if (iOn) {

                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, pi);
            } else {
                alarmManager.cancel(pi);
                pi.cancel();
            }
        }
    }

    public static Boolean isServiceAlarmOn(Context context){
        PendingIntent pi = PendingIntent.getService(context,0,PollService.newIntent(context),0);
        return pi!= null;
    }

    private Boolean isNetworkAvailableAndConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo isNetworkAvl = connectivityManager.getActiveNetworkInfo();
        if(isNetworkAvl != null && isNetworkAvl.isConnected())
            return true;
        else
            return false;

    }

}
