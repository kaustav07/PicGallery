package com.android.bignerdranch.picgallery.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.bignerdranch.picgallery.Preferance.QueryPrefarance;
import com.android.bignerdranch.picgallery.Services.PollService;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!= null && Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)){
            Boolean isOn = Boolean.getBoolean(QueryPrefarance.getAlarmStatus(context));
            PollService.setServiceAlarm(context,isOn);
        }
    }
}
