package com.draabek.androsigner;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * Subclasses Application to access context
 * Created by Vojtech Drabek on 2018-01-23.
 */

public class SignerApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    protected static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }
}
