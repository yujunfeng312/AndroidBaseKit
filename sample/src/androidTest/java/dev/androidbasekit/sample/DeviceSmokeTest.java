package dev.androidbasekit.sample;

import dev.androidbasekit.sample.view.activity.DemoActivity;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.recyclerview.widget.RecyclerView;
import dev.androidbasekit.storage.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

/**
 * DeviceSmokeTest 回归测试集。通过测试方法验证对应模块行为、错误与边界条件；测试结果见验收说明。
 */
@RunWith(AndroidJUnit4.class)
public class DeviceSmokeTest {
    @Test public void mmkvPersistsAndUserLogoutIsIsolated() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AppPreferences.initialize(context);
        String prefix = "test_" + System.nanoTime();
        AppPreferences a = AppPreferences.open(prefix + "a");
        AppPreferences b = AppPreferences.open(prefix + "b");
        try {
            a.putInt("visits", 7); b.putString("name", "b");
            assertEquals(7, AppPreferences.open(prefix + "a").getInt("visits"));
            a.clearUserData(); assertEquals(0, a.getInt("visits")); assertEquals("b", b.getString("name"));
        } finally { a.clearUserData(); b.clearUserData(); }
    }
    @Test public void mmkvCacheExpiresAndDoesNotClearPreferences() {
        String user = "ttl_test_" + System.nanoTime();
        long[] time = {100L};
        AppPreferences preferences = AppPreferences.open(user);
        PreferencesCacheStore cache = new PreferencesCacheStore(user, () -> time[0]);
        try {
            preferences.putString("setting", "kept"); cache.put("data", "cached", 20);
            assertEquals("cached", cache.get("data")); time[0] = 120; assertNull(cache.get("data"));
            cache.put("permanent", "value", 0); cache.clear();
            assertEquals("kept", preferences.getString("setting"));
        } finally { cache.clear(); preferences.clearUserData(); }
    }
    @Test public void javaSampleShowsLocalStatesAndReturnsActivityResult() throws Exception {
        prepareScreen();
        try (ActivityScenario<DemoActivity> scenario = ActivityScenario.launch(DemoActivity.class)) {
            scenario.onActivity(activity -> activity.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
            awaitLocalList();
            onView(withId(R.id.empty_button)).perform(scrollTo(), click());
            onView(withText(dev.androidbasekit.ui.R.string.basekit_empty)).check(matches(isDisplayed()));
            onView(withId(R.id.error_button)).perform(scrollTo(), click());
            onView(withText(R.string.demo_error)).check(matches(isDisplayed()));
            onView(withText(dev.androidbasekit.ui.R.string.basekit_retry)).perform(click());
            awaitLocalList();
            onView(withId(R.id.result_button)).perform(scrollTo(), click());
            onView(withId(R.id.done_button)).perform(scrollTo(), click());
            onView(withId(R.id.result_text)).perform(scrollTo()).check(matches(withText(
                InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.result_value,
                    InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.result_sample)))));
        }
    }
    @Test public void loadingTextViewHandlesClicksRestoreAndDetach() throws Exception {
        prepareScreen();
        try (ActivityScenario<DemoActivity> scenario = ActivityScenario.launch(DemoActivity.class)) {
            scenario.onActivity(activity -> {
                activity.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                android.view.ViewGroup container = activity.findViewById(android.R.id.content);
                dev.androidbasekit.ui.LoadingTextView view = new dev.androidbasekit.ui.LoadingTextView(activity);
                view.setText("Submit");
                container.addView(view, new android.view.ViewGroup.LayoutParams(300, 120));
                int[] count = {0}; view.setOnClickListener(v -> { assertTrue(view.isLoading()); count[0]++; });
                view.performClick(); view.performClick(); assertEquals(1, count[0]); assertFalse(view.isEnabled());
                android.graphics.drawable.Animatable spinner = (android.graphics.drawable.Animatable) view.getCompoundDrawablesRelative()[0];
                boolean animatorsEnabled = android.os.Build.VERSION.SDK_INT < 26 || android.animation.ValueAnimator.areAnimatorsEnabled();
                if (animatorsEnabled) assertTrue(spinner.isRunning());
                view.setText("Retry"); view.hideLoading(); assertEquals("Retry", view.getText().toString());
                assertTrue(view.isEnabled()); assertFalse(spinner.isRunning());
                view.performClick(); assertEquals(2, count[0]); container.removeView(view); assertFalse(spinner.isRunning());
                container.addView(view, new android.view.ViewGroup.LayoutParams(300, 120));
                if (animatorsEnabled) assertTrue(spinner.isRunning());
                view.hideLoading(); container.removeView(view); assertFalse(spinner.isRunning());
            });
        }
    }
    // Prepare only the test window; do not change system timeout or security settings.
    private static void prepareScreen() throws java.io.IOException {
        runShell("input keyevent 224");
        runShell("wm dismiss-keyguard"); // Only dismisses an unsecured keyguard; no credential bypass.
    }
    private static void runShell(String command) throws java.io.IOException {
        android.os.ParcelFileDescriptor descriptor = InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(command);
        try (var stream = new android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor)) {
            byte[] buffer = new byte[256];
            while (stream.read(buffer) != -1) { }
        }
    }
    private static void awaitLocalList() throws Exception {
        long deadline = System.currentTimeMillis() + 10000;
        AssertionError last = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                onView(withId(R.id.list)).check((view, error) -> {
                    if (error != null) throw error;
                    RecyclerView list = (RecyclerView) view;
                    assertTrue(list.isShown()); assertTrue(list.getAdapter().getItemCount() > 0);
                });
                return;
            } catch (AssertionError e) { last = e; Thread.sleep(50); }
        }
        throw new AssertionError("Local data did not render", last);
    }
}
