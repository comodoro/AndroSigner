package com.draabek.androsigner.pastaction;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.draabek.androsigner.R;
import com.draabek.androsigner.SignerApplication;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Recorded action of generating a new account
 * Created by Vojta on 31.12.2017
 */

public class GeneratedAddress extends PastAction {
    final String address;

    public GeneratedAddress(Date date, String appName, State state, String address) {
        super(appName, date, state);
        this.address = address;
    }
    @Override
    public String getShortDescription() {
        return address;
    }

    @Override
     public Map<String, String> getDetails() {
        Context context = SignerApplication.getContext();
        Map<String,String> myMap = new HashMap<>();
        myMap.put(context.getString(R.string.date), DateFormat.getDateInstance().format(date));
        myMap.put(context.getString(R.string.app), appName);
        myMap.put(context.getString(R.string.confirmation_state), context.getResources().getStringArray(R.array.state_types)[state.ordinal()]);
        myMap.put(context.getString(R.string.address), address);
        return myMap;
    }

    @Override
    public Drawable getIcon() {
        return SignerApplication.getContext().getResources().getDrawable(android.R.drawable.ic_input_add);
    }
}
