package com.draabek.androsigner;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.web3j.abi.datatypes.Address;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vojta on 31.12.2017.
 */

public class GeneratedAddress extends PastAction {
    Address address;

    public GeneratedAddress(Date date, String appName, Address address) {
        super(appName, date);
        this.address = address;
    }
    @Override
    public String getShortDescription() {
        return address.getValue();
    }

    @Override
     public Map<String, String> getDetails() {
        Context context = MainActivity.getContext();
        Map<String,String> myMap = new HashMap<String,String>();
        myMap.put(context.getString(R.string.date), DateFormat.getDateInstance().format(date));
        myMap.put(context.getString(R.string.app), appName);
        myMap.put(context.getString(R.string.address), address.getValue());
        return myMap;
    }

    @Override
    public Drawable getIcon() {
        return MainActivity.getContext().getResources().getDrawable(android.R.drawable.ic_input_add);
    }
}
