package com.draabek.androsigner.pastaction;

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
    protected State state;

    public PastAction(String appName, Date date, State state) {
        this.appName = appName;
        this.date = date;
        this.state = state;
    }

    public abstract String getShortDescription();
    public abstract Map<String, String> getDetails();
    public abstract Drawable getIcon();

    public Date getDate() {
        return date;
    }

    public enum State {
        CONFIRMED,
        REJECTED,
        PENDING
    }
}
