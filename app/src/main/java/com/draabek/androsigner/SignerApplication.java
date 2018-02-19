package com.draabek.androsigner;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Subclasses Application to access context
 * Created by Vojtech Drabek on 2018-01-23.
 */

public class SignerApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    protected static Context mContext;
    private static Config mConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        InputStream inputStream = getContext().getResources().openRawResource(R.raw.config);
        BufferedReader bufferedReader;
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mConfig = new Config(stringBuilder.toString());
    }

    public static Context getContext() {
        return mContext;
    }

    public static Config getConfig() {
        return mConfig;
    }
}
