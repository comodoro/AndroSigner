package com.draabek.androsigner.com.draabek.androsigner.pastaction;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.draabek.androsigner.R;
import com.draabek.androsigner.SignerApplication;

import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents recorded transaction received and confirmed by the user
 * Created by Vojta on 31.12.2017
 */

public class TransactionAction extends PastAction {
    private Transaction transaction;

    public TransactionAction(String appName, Date date, Transaction transaction) {
        super(appName, date);
        this.transaction = transaction;
    }

    @Override
    public String getShortDescription() {
        String line = SignerApplication.getContext().getString(R.string.transaction_placeholder);
        String from = transaction.getFrom().substring(0, 6) + "...";
        String to = (transaction.getTo() == null) ? "-" : transaction.getTo().substring(0, 6) + "...";
        String sValue = transaction.getValue();
        if (((sValue) != null) && (sValue.startsWith("0x"))) {
            sValue = sValue.substring(2);
        }
        BigDecimal ethValue = Convert.fromWei( new BigDecimal(new BigInteger(sValue, 16)), Convert.Unit.ETHER);
        String value = ethValue.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        return String.format(line, from, to, value);
    }

    public Map<String, String> getDetails() {
        Context context = SignerApplication.getContext();
        Map<String,String> myMap = new HashMap<>();
        myMap.put(context.getString(R.string.date), DateFormat.getDateInstance().format(date));
        myMap.put(context.getString(R.string.app), appName);
        myMap.put(context.getString(R.string.from), transaction.getFrom());
        myMap.put(context.getString(R.string.to), transaction.getTo());
        myMap.put(context.getString(R.string.value), transaction.getValue());
        return myMap;
    }

    @Override
    public Drawable getIcon() {
        Context context = SignerApplication.getContext();
        return (transaction.getData().equals("")) ? //Simple transfer?
                context.getResources().getDrawable(android.R.drawable.arrow_up_float)
        :(!(transaction.getTo() == null)) ? //Contract creation
                context.getResources().getDrawable(android.R.drawable.ic_input_get)
        : context.getResources().getDrawable(android.R.drawable.ic_input_delete);//contract call
    }
}
