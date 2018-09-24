package com.draabek.androsigner;

import android.content.Intent;

import com.example.include.Constants;

public class IntentUtils {
    public static String[] getTransactionExtras(Intent intent) {
        String password = intent.getStringExtra(Constants.INTENT_PASSWORD);
        String from = intent.getStringExtra(Constants.INTENT_FROM);
        String to = intent.getStringExtra(Constants.INTENT_TO);
        String value = intent.getStringExtra(Constants.INTENT_VALUE);
        //TODO pass unencoded function ABI (to be transparent)
        String data = intent.getStringExtra(Constants.INTENT_DATA);
        String gasPrice = intent.getStringExtra(Constants.INTENT_GAS_PRICE);
        String gasLimit = intent.getStringExtra(Constants.INTENT_GAS_LIMIT);

        return new String[]{
                password, from, to, value, data, gasPrice, gasLimit
        };
    }
}
