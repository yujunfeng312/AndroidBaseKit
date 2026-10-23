package dev.androidbasekit.sample;

import android.app.Application;
import android.os.Looper;
import java.time.Duration;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import dev.androidbasekit.sample.model.repository.LaunchPreferences;
import dev.androidbasekit.sample.view.activity.MainActivity;
import dev.androidbasekit.sample.view.activity.GuideActivity;
import dev.androidbasekit.sample.view.activity.SplashActivity;
import dev.androidbasekit.sample.view.activity.WelcomeActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** 启动分流、引导完成时机、Tab 联动和 Activity 重建回归。 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Application.class)
public class LaunchTemplateTest {
    @Before public void resetWelcome() {
        new LaunchPreferences(RuntimeEnvironment.getApplication()).setWelcomeCompleted(false);
        new LaunchPreferences(RuntimeEnvironment.getApplication()).setGuideCompleted(false);
    }

    @Test public void splashRoutesFirstAndSubsequentLaunchesAndFinishes() {
        try (var controller = Robolectric.buildActivity(SplashActivity.class).setup()) {
            assertNull(Shadows.shadowOf(controller.get()).getNextStartedActivity());
            controller.pause();
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1));
            assertNull(Shadows.shadowOf(controller.get()).getNextStartedActivity());
            controller.resume();
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1));
            assertEquals(WelcomeActivity.class.getName(), Shadows.shadowOf(controller.get())
                    .getNextStartedActivity().getComponent().getClassName());
            assertTrue(controller.get().isFinishing());
        }
        new LaunchPreferences(RuntimeEnvironment.getApplication()).setWelcomeCompleted(true);
        try (var controller = Robolectric.buildActivity(SplashActivity.class).setup()) {
            assertNull(Shadows.shadowOf(controller.get()).getNextStartedActivity());
            controller.pause();
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1));
            assertNull(Shadows.shadowOf(controller.get()).getNextStartedActivity());
            controller.resume();
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1));
            assertEquals(MainActivity.class.getName(), Shadows.shadowOf(controller.get())
                    .getNextStartedActivity().getComponent().getClassName());
            assertTrue(controller.get().isFinishing());
        }
    }

    @Test public void guideCompletesAndClosesWithoutOpeningAnotherHome() {
        LaunchPreferences preferences = new LaunchPreferences(RuntimeEnvironment.getApplication());
        try (var controller = Robolectric.buildActivity(GuideActivity.class).setup()) {
            controller.recreate();
            assertFalse(preferences.isGuideCompleted());
            GuideActivity activity = controller.get();
            activity.findViewById(R.id.start_button).performClick();
            activity.findViewById(R.id.start_button).performClick();
            assertTrue(preferences.isGuideCompleted());
            assertFalse(preferences.isWelcomeCompleted());
            assertNull(Shadows.shadowOf(activity).getNextStartedActivity());
            assertTrue(activity.isFinishing());
        }
    }

    @Test public void welcomeHasFourPagesRestoresPositionAndCompletesSeparately() {
        LaunchPreferences preferences = new LaunchPreferences(RuntimeEnvironment.getApplication());
        try (var controller = Robolectric.buildActivity(WelcomeActivity.class).setup().visible()) {
            ViewPager2 pager = controller.get().findViewById(R.id.welcome_pager);
            assertEquals(4, pager.getAdapter().getItemCount());
            controller.get().findViewById(R.id.start_button).performClick();
            assertEquals(1, pager.getCurrentItem());
            assertFalse(preferences.isWelcomeCompleted());
            assertNull(Shadows.shadowOf(controller.get()).getNextStartedActivity());
            pager.setCurrentItem(2, false);
            controller.recreate();
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            pager = controller.get().findViewById(R.id.welcome_pager);
            assertEquals(2, pager.getCurrentItem());
            assertFalse(preferences.isWelcomeCompleted());
            controller.get().findViewById(R.id.start_button).performClick();
            assertEquals(3, pager.getCurrentItem());
            assertFalse(preferences.isWelcomeCompleted());
            controller.get().findViewById(R.id.start_button).performClick();
            controller.get().findViewById(R.id.start_button).performClick();
            assertTrue(preferences.isWelcomeCompleted());
            assertFalse(preferences.isGuideCompleted());
            assertEquals(MainActivity.class.getName(), Shadows.shadowOf(controller.get())
                    .getNextStartedActivity().getComponent().getClassName());
            assertNull(Shadows.shadowOf(controller.get()).getNextStartedActivity());
            assertTrue(controller.get().isFinishing());
        }
    }

    @Test public void tabsAndPagerStayInSyncAndRestoreSelection() {
        try (var controller = Robolectric.buildActivity(MainActivity.class).setup().visible()) {
            MainActivity activity = controller.get();
            TabLayout tabs = activity.findViewById(R.id.tabs);
            ViewPager2 pager = activity.findViewById(R.id.view_pager);
            assertEquals(3, tabs.getTabCount());
            tabs.getTabAt(2).select();
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            assertEquals(2, pager.getCurrentItem());
            pager.setCurrentItem(1, false);
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            assertEquals(1, tabs.getSelectedTabPosition());
            controller.recreate();
            Shadows.shadowOf(Looper.getMainLooper()).idle();
            ViewPager2 restored = controller.get().findViewById(R.id.view_pager);
            TabLayout restoredTabs = controller.get().findViewById(R.id.tabs);
            assertEquals(1, restored.getCurrentItem());
            assertEquals(1, restoredTabs.getSelectedTabPosition());
        }
    }
}
