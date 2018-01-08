package com.draabek.androsigner.com.draabek.androsigner.pastaction;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
    private File storeDir;
    private List<PastAction> pastActionList;

    private GlobalActionsList(File storeDir) {
        this.storeDir = storeDir;
        pastActionList = new ArrayList<>();
    }

    public static void create(File storeDir) {
        if (globalActionsList != null) {
            throw new RuntimeException("Global actions list already created!");
        }
        globalActionsList = new GlobalActionsList(storeDir);
    }

    public static GlobalActionsList instance() {
        if (globalActionsList == null) {
            throw new RuntimeException("Global actions list not yet created!");
        }
        return globalActionsList;
    }

    private void writeToFile(String filename, String data) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(storeDir.getAbsolutePath() + '/' + filename);
            pw.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!(pw == null)) pw.close();
        }
    }

    @NonNull
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

    public void appendMany(List<PastAction> pastActionList) {
        for (PastAction pastAction : pastActionList) {
            append(pastAction);
        }
    }

    public void append(PastAction pastAction) {
        pastActionList.add(pastAction);
        String classString = pastAction.getClass().getName();
        String filename = classString + "_" + pastAction.getDate().getTime();
        String json = new Gson().toJson(pastAction);
        writeToFile(filename, json);
    }

    public void reloadAll() {
        pastActionList.clear();
        Gson gson = new Gson();
        File[] files = storeDir.listFiles();
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
            PastAction pastAction = (PastAction) gson.fromJson(json, c);
            pastActionList.add(pastAction);
        }
    }

    public List<PastAction> getPastActionList() {
        return pastActionList;
    }
}
