package com.draabek.androsigner;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * List of all user accounts
 * Created by Vojtech Drabek on 2018-01-11.
 */

class GlobalAccountManager {

    private static GlobalAccountManager globalAccountManager;

    private final File rootDir;
    private final Map<String, String> savedAccounts;

    public static void create(File rootDir) {
        if (globalAccountManager != null) {
            throw new RuntimeException("Global account manager already created!");
        }
        globalAccountManager = new GlobalAccountManager(rootDir);
    }

    public static GlobalAccountManager instance() {
        if (globalAccountManager == null) {
            throw new RuntimeException("Global account manager not yet created!");
        }
        return globalAccountManager;
    }

    private GlobalAccountManager(File rootDir) {
        savedAccounts = new HashMap<>();
        this.rootDir = rootDir;
    }

    public Credentials getCredentials(String address, String password) {
        String source = savedAccounts.get(address);
        if (source == null) return null;
        try {
            return WalletUtils.loadCredentials(password, source);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void reloadAll() {
        savedAccounts.clear();
        File[] files = rootDir.listFiles();
        for (File file : files) {
            String filename = file.getName();
            String[] chunks = filename.split("--");
            if ((chunks.length == 3) && (chunks[2].endsWith("json"))) {
                savedAccounts.put("0x" + chunks[2].substring(0, chunks[2].length() - 5), file.getAbsolutePath());
            }
        }
    }

    public Collection<String> getAddresses() {
        return savedAccounts.keySet();
    }

    public File getRootDir() {
        return rootDir;
    }
}
