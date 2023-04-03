package com.bumbumapps.statusdownloader.utils;

import android.content.Context;

public interface Observer {
    void update(final String value, Context context);
}