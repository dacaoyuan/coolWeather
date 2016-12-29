package com.coolweather.app.activity;

import android.app.Application;

import org.xutils.x;

/**
 * Created by abc on 2016/9/5.
 */
public class InitActivity extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
    }
}
