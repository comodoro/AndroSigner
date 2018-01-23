package com.draabek.androsigner.com.draabek.androsigner.pastaction;

import android.support.annotation.NonNull;
import android.util.Log;

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
    private File rootDir;
    private List<PastAction> pastActionList;

    private GlobalActionsList(File rootDir) {
        this.rootDir = rootDir;
        pastActionList = new ArrayList<>();
    }

    public static void create(File storeDir) {
        if (globalActionsList != null) {
            if (storeDir.equals(GlobalActionsList.instance().rootDir)) {
                //throw new RuntimeException("Global actions list already created with the same root dir!");
                Log.w(GlobalActionsList.class.getName(), "Global actions list already created with the same root dir!");
            }
            throw new RuntimeException("Global actions list already created with different root dir!");
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
            pw = new PrintWriter(rootDir.getAbsolutePath() + '/' + filename);
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
        File[] files = rootDir.listFiles();
        for (File file : files) {
            String name = file.getName();
            String className = name.substring(0, name.indexOf('_'));
            Class<?> c = null;
            try {
                c = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (c == null) continue;
            String json = readStringFromFile(file);
            PastAction pastAction = (PastAction) gson.fromJson(json, c);
            pastActionList.add(pastAction);
        }
    }

    public File getRootDir() {
        return rootDir;
    }

    public List<PastAction> getPastActionList() {
        return pastActionList;
    }
}
