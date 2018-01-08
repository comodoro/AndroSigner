package com.draabek.androsigner.com.draabek.androsigner.pastaction;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.draabek.androsigner.MainActivity;
import com.draabek.androsigner.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vojta on 31.12.2017.
 */

public class GeneratedAddress extends PastAction {
    String address;

    public GeneratedAddress(Date date, String appName, String address) {
        super(appName, date);
        this.address = address;
    }
    @Override
    public String getShortDescription() {
        return address;
    }

    @Override
     public Map<String, String> getDetails() {
        Context context = MainActivity.getContext();
        Map<String,String> myMap = new HashMap<String,String>();
        myMap.put(context.getString(R.string.date), DateFormat.getDateInstance().format(date));
        myMap.put(context.getString(R.string.app), appName);
        myMap.put(context.getString(R.string.address), address);
        return myMap;
    }

    @Override
    public Drawable getIcon() {
        return MainActivity.getContext().getResources().getDrawable(android.R.drawable.ic_input_add);
    }
}
