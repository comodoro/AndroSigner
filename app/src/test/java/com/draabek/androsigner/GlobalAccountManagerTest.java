package com.draabek.androsigner;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test the account list
 * Created by Vojtech Drabek on 2018-01-12.
 */
public class GlobalAccountManagerTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws Exception {
        GlobalAccountManager.create(folder.getRoot());

    }
    @Test
    public void getCredentials() throws Exception {
        String source = WalletUtils.generateFullNewWalletFile("1234", folder.getRoot());
        File sourceFile = new File(folder.getRoot(), source);
        Credentials credentials = WalletUtils.loadCredentials("1234", sourceFile);
        Assert.assertNotNull(credentials);
    }

    @Test
    public void reloadAll() throws Exception {
        String[] passwords = new String[] {"a", "b", "c", "d"};
        List<String> addrs = new ArrayList<>();
        for (String s : passwords) {
            String filename = WalletUtils.generateFullNewWalletFile(s, folder.getRoot());
            File sourceFile = new File(folder.getRoot(), filename);
            Credentials credentials = WalletUtils.loadCredentials(s, sourceFile);
            addrs.add(credentials.getAddress());
        }
        GlobalAccountManager.instance().reloadAll();
        Collection<String> loaded = GlobalAccountManager.instance().getAddresses();
        for (int i = 0;i < addrs.size();i++) {
            Assert.assertTrue(loaded.contains(addrs.get(i)));
        }
    }

}