package com.draabek.androsigner;

/**
 * Actions to be performed on confirm or reject of received action
 * Created by Vojtech Drabek on 2018-01-10.
 */

public interface ConfirmAction {
    void confirm();
    void reject();
}
