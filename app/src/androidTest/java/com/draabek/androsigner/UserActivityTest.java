package com.draabek.androsigner;


import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.draabek.androsigner.com.draabek.androsigner.pastaction.GlobalActionsList;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.AdminFactory;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.ActivityResultMatchers.hasResultCode;
import static android.support.test.espresso.contrib.ActivityResultMatchers.hasResultData;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class UserActivityTest {

    @ClassRule
    public static TemporaryFolder actionsFolder = new TemporaryFolder();
    @ClassRule
    public static TemporaryFolder accountsFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUp() {
        GlobalActionsList.create(actionsFolder.getRoot());
        GlobalAccountManager.create(accountsFolder.getRoot());

    }

    @Rule
    public MyCustomRule<UserActionActivity> mActivityTestRule = new MyCustomRule<>(
            UserActionActivity.class, true, false);

    private String getUnlockedAddress() throws IOException {
        Admin admin = AdminFactory.build(new HttpService("http://10.0.2.2:8545"));
        List<String> accounts = admin.personalListAccounts().send().getAccountIds();
        return accounts.get(0);
    }

    @Test
    public void userActivityTestGenerateAddress() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Constants.INTENT_KEY_COMMAND, Constants.COMMAND_GENERATE_ADDRESS);
        intent.putExtra(Constants.INTENT_PASSWORD, "12345");
        intent.setPackage("com.sample.test");
        mActivityTestRule.launchActivity(intent);
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.user_action_yes),
                        isDisplayed()));
        actionMenuItemView.perform(click());
        assertThat(mActivityTestRule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
        assertThat(mActivityTestRule.getActivityResult(),
                hasResultData(IntentMatchers.hasExtraWithKey(Constants.RETURN_GENERATED_ADDRESS)));
        String address = mActivityTestRule.getActivityResult()
                .getResultData().getStringExtra(Constants.RETURN_GENERATED_ADDRESS);
        Assert.assertTrue(GlobalAccountManager.instance().getAddresses().contains(address));

    }
    @Test
    public void userActivityTestTransaction() throws IOException {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Constants.INTENT_KEY_COMMAND, Constants.COMMAND_CONFIRM_TRANSACTION);
        intent.putExtra(Constants.INTENT_PASSWORD, "12345");
        String from = getUnlockedAddress();
        intent.putExtra(Constants.INTENT_FROM, from);
        intent.putExtra(Constants.INTENT_TO, "12345");
        intent.putExtra(Constants.INTENT_DATA, "12345");
        intent.putExtra(Constants.INTENT_GAS_LIMIT, 200000);
        intent.putExtra(Constants.INTENT_GAS_PRICE, 0);
        intent.setPackage("com.sample.test");
        mActivityTestRule.launchActivity(intent);
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.user_action_yes),
                        isDisplayed()));
        actionMenuItemView.perform(click());
        assertThat(mActivityTestRule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
        assertThat(mActivityTestRule.getActivityResult(),
                hasResultData(IntentMatchers.hasExtraWithKey(Constants.RETURN_GENERATED_ADDRESS)));
        String address = mActivityTestRule.getActivityResult()
                .getResultData().getStringExtra(Constants.RETURN_GENERATED_ADDRESS);
        Assert.assertTrue(GlobalAccountManager.instance().getAddresses().contains(address));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    static class MyCustomRule<A extends UserActionActivity> extends ActivityTestRule<A> {
        public MyCustomRule(Class<A> activityClass, boolean b1, boolean b2) {
            super(activityClass, b1, b2);
        }

        @Override
        protected void afterActivityLaunched() {
            super.afterActivityLaunched();
            SignerApplication.getConfig().setEndpoint("http://10.0.2.2:8545");
        }
    }
}
