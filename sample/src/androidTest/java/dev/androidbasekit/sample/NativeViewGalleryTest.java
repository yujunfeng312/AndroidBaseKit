package dev.androidbasekit.sample;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dev.androidbasekit.sample.view.activity.NativeViewDemoActivity;
import dev.androidbasekit.sample.view.activity.NativeViewsActivity;
import dev.androidbasekit.sample.view.nativeviews.NativeViewCatalog;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

/** 在真实系统上遍历控件页面，捕捉样式构造、窗口附着及销毁时的兼容问题。 */
@RunWith(AndroidJUnit4.class)
public class NativeViewGalleryTest {
    @Test public void everyCatalogEntryOpensAndContainsItsFrameworkView() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        for (NativeViewCatalog.Entry entry : NativeViewCatalog.ALL) {
            Intent intent = new Intent(context, NativeViewDemoActivity.class)
                    .putExtra(NativeViewDemoActivity.EXTRA_VIEW, entry.name);
            try (ActivityScenario<NativeViewDemoActivity> scenario = ActivityScenario.launch(intent)) {
                Class<?> type = Class.forName(entry.name.equals("MediaController") ? "android.widget.VideoView"
                        : entry.name.equals("WebView") ? "android.webkit.WebView"
                        : java.util.Arrays.asList("View", "SurfaceView", "TextureView", "ViewStub").contains(entry.name)
                        ? "android.view." + entry.name : "android.widget." + entry.name);
                scenario.onActivity(activity -> {
                    assertFalse(entry.name, activity.isFinishing());
                    assertEquals("zh", activity.getResources().getConfiguration().getLocales().get(0).getLanguage());
                    assertEquals(activity.getString(entry.titleRes), ((TextView) activity.findViewById(R.id.title)).getText().toString());
                    assertNotNull(entry.name, find(activity.findViewById(R.id.examples), type));
                    if (entry.name.equals("MediaController")) {
                        findButton(activity.findViewById(R.id.examples), activity.getString(R.string.native_play)).performClick();
                    }
                });
                if (entry.name.equals("MediaController")) {
                    // MediaController 由系统添加到独立浮动 Window，不属于 Activity 的 View 树。
                    onView(isAssignableFrom(MediaController.class)).check(matches(isDisplayed()));
                }
            }
        }
    }

    @Test public void searchRoutesToMatchingIndependentPage() {
        try (ActivityScenario<NativeViewsActivity> scenario = ActivityScenario.launch(NativeViewsActivity.class)) {
            scenario.onActivity(activity -> {
                ((SearchView) activity.findViewById(R.id.search)).setQuery("TextView", false);
                ListView catalog = activity.findViewById(R.id.catalog);
                assertEquals(5, catalog.getCount()); // 一个分类标题 + 四个控件。
                assertFalse(catalog.getAdapter().isEnabled(0));
                assertTrue(catalog.getItemAtPosition(1).toString().contains("TextView"));
                ((SearchView) activity.findViewById(R.id.search)).setQuery("文本控件", false);
                assertEquals(2, catalog.getCount());
                assertTrue(catalog.getItemAtPosition(1).toString().contains("TextView"));
                ((SearchView) activity.findViewById(R.id.search)).setQuery("not-a-widget", false);
                assertEquals(0, catalog.getCount());
            });
        }
    }

    @Test public void commonPropertiesAndInputSurviveRecreation() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, NativeViewDemoActivity.class).putExtra(NativeViewDemoActivity.EXTRA_VIEW, "EditText");
        try (ActivityScenario<NativeViewDemoActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                ViewGroup examples = activity.findViewById(R.id.examples);
                ((EditText) find(examples, EditText.class)).setText("Hello");
                ((SeekBar) find(examples, SeekBar.class)).setProgress(60);
                ((Switch) find(examples, Switch.class)).setChecked(false);
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                ViewGroup examples = activity.findViewById(R.id.examples);
                EditText input = (EditText) find(examples, EditText.class);
                assertEquals("Hello", input.getText().toString());
                assertEquals(.6f, input.getAlpha(), .01f);
                assertFalse(input.isEnabled());
            });
        }
    }

    private static View find(View root, Class<?> type) {
        if (type.isInstance(root)) return root;
        if (root instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) root;
            for (int i = 0; i < parent.getChildCount(); i++) {
                View match = find(parent.getChildAt(i), type);
                if (match != null) return match;
            }
        }
        return null;
    }

    private static Button findButton(View root, String text) {
        if (root instanceof Button && ((Button) root).getText().toString().equals(text)) return (Button) root;
        if (root instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) root;
            for (int i = 0; i < parent.getChildCount(); i++) {
                Button button = findButton(parent.getChildAt(i), text);
                if (button != null) return button;
            }
        }
        return null;
    }
}
