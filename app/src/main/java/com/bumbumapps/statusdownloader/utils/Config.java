package com.bumbumapps.statusdownloader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.provider.MediaStore;

public class Config {
    public static final String WhatsAppDirectoryPath = android.os.Build.VERSION.SDK_INT < 30 ? Environment.getExternalStorageDirectory().getAbsolutePath()+ "/WhatsApp/Media/.Statuses/" : Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/media/com.whatsapp/WhatsApp/Media/.Statuses/";
    public static final String WhatsAppSaveStatus = "/storage/emulated/0/WhatsAppStatusesDir/Media/WhatsApp/";
    public static final String GBWhatsAppDirectoryPath = "/storage/emulated/0/GWAActivity/Media/.Statuses/";
    public static final String GBWhatsAppSaveStatus = "/storage/emulated/0/WhatsAppStatusesDir/Media/GWAActivity/";
    public static final String WhatsAppBusinessDirectoryPath = "/storage/emulated/0/WhatsApp Business/Media/.Statuses/";
    public static final String WhatsAppBusinessSaveStatus = "/storage/emulated/0/WhatsAppStatusesDir/Media/WhatsAppBusiness/";

    public static final int count = 6;

    public static String getALLState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("PREFRENCE", Context.MODE_PRIVATE);
        if (prefs.getString("ALL", "").length() > 0) {
            return prefs.getString("ALL", "");
        } else
            return "";
    }
}
