package com.android.bignerdranch.picgallery.Preferance;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Kaustav on 07-12-2017.
 */

public class QueryPrefarance {

    private static final String SEARCH_PREF_KEY = "searchPref";
    private static final String LAST_RESULT_ID = "lastResultId";
    private static final String ALARM_STATUS = "alarmstatus";

    public static void setQueryInPrefarance(String query, Context context){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SEARCH_PREF_KEY,query).apply();
    }

    public static String getQueryFromPreference(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(SEARCH_PREF_KEY,null);
    }

    public static void setLastResultId(String lastid, Context context){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(LAST_RESULT_ID,lastid).apply();
    }

    public static String getLastResultId(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(LAST_RESULT_ID,null);
    }

    public static void setAlarmStatus(Boolean status, Context context){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(ALARM_STATUS,status.toString()).apply();
    }

    public static String getAlarmStatus(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(ALARM_STATUS,null);
    }
}
