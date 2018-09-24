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
 * Represents recorded presenting of the account list to a calling app
 * Created by Vojta on 2018-04-12
 */

public class AccountListAction extends PastAction {

    public AccountListAction(String originActivity, Date date, State state) {
        super(originActivity, date, state);
    }

    @Override
    public String getShortDescription() {
        String line = "[]";
        return line;
    }

    public Map<String, String> getDetails() {
        Context context = SignerApplication.getContext();
        Map<String,String> myMap = new HashMap<>();
        myMap.put(context.getString(R.string.date), DateFormat.getDateInstance().format(date));
        myMap.put(context.getString(R.string.app), appName);
        myMap.put(context.getString(R.string.state), context.getResources().getStringArray(R.array.state_types)[state.ordinal()]);
        return myMap;
    }

    @Override
    public Drawable getIcon() {
        Context context = SignerApplication.getContext();
        return context.getResources().getDrawable(android.R.drawable.ic_menu_agenda);
    }
}
