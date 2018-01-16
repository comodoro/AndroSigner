package com.draabek.androsigner;

import com.draabek.androsigner.com.draabek.androsigner.pastaction.PastAction;

/**
 * Actions to be performed on confirm or reject of received action
 * Created by Vojtech Drabek on 2018-01-10.
 */

public interface ConfirmAction {
    void confirm(PastAction pastAction);
    void reject(PastAction pastAction);
}
