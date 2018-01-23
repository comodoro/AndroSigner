package com.draabek.androsigner.com.draabek.androsigner.pastaction;

import android.graphics.drawable.Drawable;

import java.util.Date;
import java.util.Map;

/**
 * A base class for action sent to the signer and confirmed by the user
 * Created by Vojta on 30.12.2017.
 */

public abstract class PastAction {
    protected Date date;
    protected String appName;
    public PastAction(String appName, Date date) {
        this.appName = appName;
        this.date = date;
    }

    public abstract String getShortDescription();
    public abstract Map<String, String> getDetails();
    public abstract Drawable getIcon();

    public Date getDate() {
        return date;
    }
}
