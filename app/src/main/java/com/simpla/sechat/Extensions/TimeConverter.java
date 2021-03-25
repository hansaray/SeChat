package com.simpla.sechat.Extensions;

import android.content.Context;

import com.simpla.sechat.R;

public class TimeConverter {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    /*public static String getTimeAgo(long time, Context context) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return context.getResources().getString(R.string.now);
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return context.getResources().getString(R.string.just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return context.getResources().getString(R.string.AminuteAgo);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS +" "+ context.getResources().getString(R.string.MinutesAgo);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return context.getResources().getString(R.string.AnHourAgo);
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS +" "+ context.getResources().getString(R.string.HoursAgo);
        } else if (diff < 48 * HOUR_MILLIS) {
            return context.getResources().getString(R.string.yesterday);
        } else {
            return diff / DAY_MILLIS +" "+ context.getResources().getString(R.string.DaysAgo);
        }
    }*/

    static long getTimeAgoNotification(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }
        long now = System.currentTimeMillis();
        final long diff = now - time;
        return diff / DAY_MILLIS;
    }

    public static String getTimeAgoForShorterTime(long time, Context context){
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return context.getResources().getString(R.string.now);
        }
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return context.getResources().getString(R.string.just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return context.getResources().getString(R.string.one_m);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + context.getResources().getString(R.string.m);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return context.getResources().getString(R.string.one_h);
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + context.getResources().getString(R.string.h);
        } else if (diff < 48 * HOUR_MILLIS) {
            return context.getResources().getString(R.string.yesterday);
        } else {
            return diff / DAY_MILLIS + context.getResources().getString(R.string.d);
        }
    }

    static long getTimeInfo(long time){
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return 1;
        }
        final long diff = now - time;
        if(diff < 48 * HOUR_MILLIS) {
            return 1;
        }else {
            return diff / DAY_MILLIS;
        }
    }
}
