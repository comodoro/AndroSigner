package com.draabek.androsigner;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vojta on 31.12.2017.
 */

public class TransactionAction extends PastAction {
    private Transaction transaction;

    public TransactionAction(String appName, Date date, Transaction transaction) {
        super(appName, date);
        this.transaction = transaction;
    }

    @Override
    public String getShortDescription() {
        String line = MainActivity.getContext().getString(R.string.transaction_placeholder);
        String from = transaction.getFrom().substring(0, 7) + "...";
        String to = (transaction.getTo() == null) ? "-" : transaction.getTo().substring(0, 7) + "...";
        BigDecimal ethValue = Convert.fromWei( transaction.getValue().toString(), Convert.Unit.ETHER);
        String value = ethValue.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        return String.format(line, from, to, value);
    }

    public Map<String, String> getDetails() {
        Context context = MainActivity.getContext();
        Map<String,String> myMap = new HashMap<String,String>();
        myMap.put(context.getString(R.string.date), DateFormat.getDateInstance().format(date));
        myMap.put(context.getString(R.string.app), appName);
        myMap.put(context.getString(R.string.from), transaction.getFrom());
        myMap.put(context.getString(R.string.to), transaction.getTo());
        myMap.put(context.getString(R.string.value), transaction.getValue().toString());
        return myMap;
    }

    @Override
    public Drawable getIcon() {
        Context context = MainActivity.getContext();
        return (transaction.getInput().equals("")) ? //Simple transfer?
                context.getResources().getDrawable(android.R.drawable.arrow_up_float)
        :(!transaction.getCreates().equals("")) ? //Contract creation
                context.getResources().getDrawable(android.R.drawable.ic_input_get)
        : context.getResources().getDrawable(android.R.drawable.ic_input_delete);//contract call
    }
}
