package dev.androidbasekit.sample;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;
import dev.androidbasekit.ui.Clicks;
import dev.androidbasekit.ui.LoadingTextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import android.app.Application;
import static org.junit.Assert.*;

/**
 * LoadingTextViewTest 回归测试集。通过测试方法验证对应模块行为、错误与边界条件；测试结果见验收说明。
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Application.class)
public class LoadingTextViewTest {
    @Test public void clickStartsLoadingBeforeCallbackAndBlocksDuplicates() {
        try (var controller = Robolectric.buildActivity(JavaInteropTest.TestActivity.class).setup()) {
            LoadingTextView view = new LoadingTextView(controller.get()); view.setText("Submit");
            int[] count = {0};
            view.setOnClickListener(v -> { assertTrue(view.isLoading()); count[0]++; });
            assertTrue(view.performClick()); assertFalse(view.isEnabled());
            assertFalse(view.performClick()); assertEquals(1, count[0]);
            view.hideLoading(); assertEquals("Submit", view.getText().toString()); assertTrue(view.isEnabled());
            assertTrue(view.performClick()); assertEquals(2, count[0]); view.hideLoading();
        }
    }
    @Test public void loadingTextAndNormalTextCanBeUpdatedIndependently() {
        try (var controller = Robolectric.buildActivity(JavaInteropTest.TestActivity.class).setup()) {
            LoadingTextView view = new LoadingTextView(controller.get()); view.setText("Submit"); view.showLoading();
            view.setText("Retry"); view.setLoadingText("Please wait");
            assertEquals("Please wait", view.getText().toString());
            view.hideLoading(); assertEquals("Retry", view.getText().toString());
        }
    }
    @Test public void disabledStateIsPreservedAndCannotBeEnabledWhileLoading() {
        try (var controller = Robolectric.buildActivity(JavaInteropTest.TestActivity.class).setup()) {
            LoadingTextView view = new LoadingTextView(controller.get()); view.setEnabled(false);
            view.showLoading(); view.hideLoading(); assertFalse(view.isEnabled());
            view.showLoading(); view.setEnabled(true); assertFalse(view.isEnabled());
            view.hideLoading(); assertTrue(view.isEnabled());
        }
    }
    @Test public void originalSpansDrawablesPaddingAndWidthAreRestored() {
        try (var controller = Robolectric.buildActivity(JavaInteropTest.TestActivity.class).setup()) {
            LoadingTextView view = new LoadingTextView(controller.get());
            SpannableString label = new SpannableString("Submit");
            StyleSpan span = new StyleSpan(android.graphics.Typeface.BOLD);
            label.setSpan(span, 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            view.setText(label, TextView.BufferType.SPANNABLE);
            Drawable original = new ColorDrawable(android.graphics.Color.RED); original.setBounds(0, 0, 12, 12);
            view.setCompoundDrawablesRelative(original, null, null, null);
            view.setCompoundDrawablePadding(3); view.setMinimumWidth(24); view.layout(0, 0, 180, 60);
            view.showLoading(); assertTrue(view.getMinimumWidth() >= 180); view.hideLoading();
            assertSame(original, view.getCompoundDrawablesRelative()[0]); assertEquals(3, view.getCompoundDrawablePadding());
            assertEquals(24, view.getMinimumWidth());
            assertSame(span, ((Spanned) view.getText()).getSpans(0, label.length(), StyleSpan.class)[0]);
        }
    }
    @Test public void manualModeSupportsDebounceWithoutStickingInLoading() {
        try (var controller = Robolectric.buildActivity(JavaInteropTest.TestActivity.class).setup()) {
            LoadingTextView view = new LoadingTextView(controller.get()); view.setAutoLoadingOnClick(false);
            int[] count = {0}; Clicks.setDebounced(view, () -> count[0]++);
            view.performClick(); view.performClick(); assertEquals(1, count[0]); assertFalse(view.isLoading());
            view.setLoading(true); assertFalse(view.performClick()); view.setLoading(false);
        }
    }
    @Test public void listenerExceptionDoesNotLeaveLoadingBehind() {
        try (var controller = Robolectric.buildActivity(JavaInteropTest.TestActivity.class).setup()) {
            LoadingTextView view = new LoadingTextView(controller.get()); view.setText("Submit");
            view.setOnClickListener(v -> { throw new IllegalStateException("failure"); });
            try { view.performClick(); fail("Listener failure swallowed"); } catch (IllegalStateException expected) { }
            assertFalse(view.isLoading()); assertTrue(view.isEnabled()); assertEquals("Submit", view.getText().toString());
        }
    }
    @Test public void hiddenWindowPausesLoadingAndDetachStopsTheDrawable() {
        try (var controller = Robolectric.buildActivity(JavaInteropTest.TestActivity.class).setup()) {
            LoadingTextView view = new LoadingTextView(controller.get()); controller.get().setContentView(view);
            controller.visible().pause().stop();
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            assertEquals(android.view.View.GONE, view.getWindowVisibility());
            view.showLoading(); Animatable spinner = (Animatable) view.getCompoundDrawablesRelative()[0];
            assertFalse(spinner.isRunning());
            // Simulate a drawable that was active before removal; the widget owns cleanup.
            spinner.start();
            assertTrue(spinner.isRunning());
            controller.get().setContentView(new TextView(controller.get())); assertFalse(spinner.isRunning());
            controller.get().setContentView(view); assertFalse(spinner.isRunning());
            view.hideLoading(); assertFalse(spinner.isRunning());
        }
    }
}
