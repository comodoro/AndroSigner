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
 * Represents recorded transaction received and confirmed by the user
 * Created by Vojta on 2018-04-12
 */

public class SignMessageAction extends PastAction {

    String message;
    String signingAccount;

    public SignMessageAction(String originActivity, Date date, State state, String message, String signingAccount) {
        super(originActivity, date, state);
        this.message = message;
        this.signingAccount = signingAccount;
    }

    @Override
    public String getShortDescription() {
        String abbrMess = message.length() > 20 ? message.substring(0, 6) + "..." : message;
        String abbrAddr = signingAccount.substring(0, 8) + "...";
        return String.format("Sign %s with %s", abbrMess, abbrAddr);
    }

    public Map<String, String> getDetails() {
        Context context = SignerApplication.getContext();
        Map<String,String> myMap = new HashMap<>();
        myMap.put(context.getString(R.string.date), DateFormat.getDateInstance().format(date));
        myMap.put(context.getString(R.string.app), appName);
        myMap.put(context.getString(R.string.state), context.getResources().getStringArray(R.array.state_types)[state.ordinal()]);
        myMap.put(context.getString(R.string.sign_message_action_message), message);
        myMap.put(context.getString(R.string.address), signingAccount);
        return myMap;
    }

    @Override
    public Drawable getIcon() {
        Context context = SignerApplication.getContext();
        return context.getResources().getDrawable(android.R.drawable.ic_menu_set_as);
    }
}
