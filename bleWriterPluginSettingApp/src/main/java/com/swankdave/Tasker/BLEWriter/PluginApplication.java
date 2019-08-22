package com.swankdave.Tasker.BLEWriter;

import com.twofortyfouram.log.Lumberjack;
import android.app.Application;

/**
 * Implements an application object for the plug-in.
 */
/*
 * This application is non-essential for the plug-in's operation; it simply enables debugging
 * options globally for the app.
 */
public final class PluginApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Lumberjack.init(getApplicationContext());
    }
}
