package com.draabek.androsigner;

import com.draabek.androsigner.com.draabek.androsigner.pastaction.GeneratedAddress;
import com.draabek.androsigner.com.draabek.androsigner.pastaction.GlobalActionsList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Date;

/**
 * Created by Vojtech Drabek on 2018-01-08
 *
 */
public class GlobalActionsListTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws Exception {
        GlobalActionsList.create(folder.getRoot());
        GlobalActionsList.instance().append(new GeneratedAddress(new Date(), "debug", "0x123"));
    }

    @Test
    public void append() throws Exception {
        GlobalActionsList.instance().append(new GeneratedAddress(new Date(), "debug", "0x234"));
        Assert.assertEquals(2, GlobalActionsList.instance().getPastActionList().size());
        Assert.assertEquals(2, folder.getRoot().listFiles().length);
    }

    @Test
    public void reloadAll() throws Exception {
        GlobalActionsList.instance().getPastActionList().clear();
        GlobalActionsList.instance().reloadAll();
        Assert.assertEquals(2, GlobalActionsList.instance().getPastActionList().size());
    }


}