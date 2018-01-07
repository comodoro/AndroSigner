package com.draabek.androsigner;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vojtech Drabek on 2018-01-06.
 *
 */

public class GlobalActionsList {
    private static GlobalActionsList globalActionsList = null;
    private List<TransactionAction> transactionActionList;

    private GlobalActionsList() {
        transactionActionList = new ArrayList<>();
    }

    public static GlobalActionsList instance() {
        if (globalActionsList == null)
            globalActionsList = new GlobalActionsList();
        return globalActionsList;
    }

    private void writeToFile(String filename, String data) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filename);
            pw.print(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (!(pw == null)) pw.close();
        }
    }

    private String readStringFromFile(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public void appendMany(List<TransactionAction> transactionActionList) {
        for (TransactionAction transactionAction : transactionActionList) {
            append(transactionAction);
        }
    }

    public void append(TransactionAction transactionAction) {
        String classString = transactionAction.getClass().getName();
        String filename = classString + "_" + transactionAction.getDate().toString();
        String json = new Gson().toJson(transactionAction);
        writeToFile(filename, json);
    }

    public void load(Context ctx) {
        Gson gson = new Gson();
        File[] files = ctx.getFilesDir().listFiles();
        for (File file : files) {
            String name = file.getName();
            String className = name.substring(0, name.indexOf('_'));
            Class c = null;
            try {
                c = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String json = readStringFromFile(file);
            TransactionAction transactionAction = (TransactionAction) gson.fromJson(json, c);
            transactionActionList.add(transactionAction);
        }
    }
}
