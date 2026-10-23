package dev.androidbasekit.sample;

import android.app.Application;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import dev.androidbasekit.common.ByteSizes;
import dev.androidbasekit.common.Durations;
import dev.androidbasekit.common.WebUrls;
import dev.androidbasekit.ui.TextSpans;
import dev.androidbasekit.ui.ViewLayouts;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Application.class)
public class PortableUiToolsTest {
    @Test public void commonDefaultsAreCallableFromJava() {
        assertEquals("02:05", Durations.formatSeconds(125));
        assertEquals("1.00 KiB", ByteSizes.format(1024));
        assertTrue(WebUrls.isHttpUrl("https://example.com"));
    }

    @Test public void colorsPreserveExistingStyleWithoutMutatingSource() {
        SpannableString original = new SpannableString("ab ab");
        StyleSpan bold = new StyleSpan(Typeface.BOLD);
        original.setSpan(bold, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString colored = TextSpans.colorMatches(original, "ab", Color.RED);
        assertEquals(2, colored.getSpans(0, colored.length(), ForegroundColorSpan.class).length);
        assertEquals(0, original.getSpans(0, original.length(), ForegroundColorSpan.class).length);
        assertEquals(0, colored.getSpanStart(bold));
        assertEquals(2, colored.getSpanEnd(bold));
        assertEquals(0, TextSpans.colorMatches(original, "", Color.RED)
            .getSpans(0, original.length(), ForegroundColorSpan.class).length);
        assertThrows(IllegalArgumentException.class, () -> TextSpans.colorRange(original, 0, 6, Color.RED));
    }

    @Test public void layoutPreservesParentSpecificParamsAndRelativePadding() {
        View view = new View(RuntimeEnvironment.getApplication());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, 20, 1f);
        view.setLayoutParams(params);
        ViewLayouts.setSize(view, ViewGroup.LayoutParams.MATCH_PARENT, 30);
        ViewLayouts.setMargins(view, 1, 2, 3, 4);
        assertSame(params, view.getLayoutParams());
        assertEquals(1f, params.weight, 0f);
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(30, params.height);
        assertEquals(1, params.getMarginStart());
        assertEquals(3, params.getMarginEnd());
        assertEquals(2, params.topMargin);
        assertEquals(4, params.bottomMargin);
        view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        view.setPaddingRelative(5, 6, 7, 8);
        ViewLayouts.setBottomPadding(view, 9);
        assertEquals(5, view.getPaddingStart());
        assertEquals(6, view.getPaddingTop());
        assertEquals(7, view.getPaddingEnd());
        assertEquals(9, view.getPaddingBottom());
        assertThrows(IllegalArgumentException.class, () -> ViewLayouts.setSize(view, -3, 30));
    }

    @Test public void preDrawRunsOnceAndCanBeCancelled() {
        View view = new View(RuntimeEnvironment.getApplication());
        int[] calls = {0};
        ViewLayouts.onNextPreDraw(view, () -> calls[0]++);
        view.getViewTreeObserver().dispatchOnPreDraw();
        view.getViewTreeObserver().dispatchOnPreDraw();
        assertEquals(1, calls[0]);
        ViewLayouts.onNextPreDraw(view, () -> calls[0]++).removeListener();
        view.getViewTreeObserver().dispatchOnPreDraw();
        assertEquals(1, calls[0]);
    }
}
