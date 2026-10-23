package dev.androidbasekit.sample;

import android.app.Application;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Spinner;
import androidx.appcompat.widget.SwitchCompat;
import dev.androidbasekit.sample.view.activity.SystemUiDemoActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** 只验证窗口状态与生命周期恢复；系统栏的实际绘制和手势效果留给设备检查。 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Application.class)
public class SystemUiDemoTest {
    @Test public void brightnessIsWindowScopedAndResetRestoresSystemDefaults() {
        try (var controller = Robolectric.buildActivity(SystemUiDemoActivity.class).setup()) {
            SystemUiDemoActivity activity = controller.get();
            int systemBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
            ((SwitchCompat) activity.findViewById(R.id.follow_brightness)).setChecked(false);
            ((SeekBar) activity.findViewById(R.id.brightness)).setProgress(23);
            ((SwitchCompat) activity.findViewById(R.id.keep_awake)).setChecked(true);
            assertEquals(.23f, activity.getWindow().getAttributes().screenBrightness, .001f);
            assertTrue((activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0);
            assertEquals(systemBrightness, Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1));
            activity.findViewById(R.id.reset).performClick();
            assertEquals(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE,
                    activity.getWindow().getAttributes().screenBrightness, .001f);
            assertFalse((activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0);
        }
    }

    @Test public void recreationKeepsColorAndWindowOptions() {
        try (var controller = Robolectric.buildActivity(SystemUiDemoActivity.class).setup()) {
            SystemUiDemoActivity activity = controller.get();
            ((Spinner) activity.findViewById(R.id.status_color)).setSelection(2);
            ((SwitchCompat) activity.findViewById(R.id.follow_brightness)).setChecked(false);
            ((SeekBar) activity.findViewById(R.id.brightness)).setProgress(67);
            ((SwitchCompat) activity.findViewById(R.id.keep_awake)).setChecked(true);
            activity.findViewById(R.id.immersive).performClick();
            controller.recreate();
            activity = controller.get();
            assertEquals(2, ((Spinner) activity.findViewById(R.id.status_color)).getSelectedItemPosition());
            assertTrue(((SwitchCompat) activity.findViewById(R.id.hide_status)).isChecked());
            assertTrue(((SwitchCompat) activity.findViewById(R.id.hide_navigation)).isChecked());
            assertEquals(.67f, activity.getWindow().getAttributes().screenBrightness, .001f);
            assertTrue((activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0);
            activity.findViewById(R.id.show_bars).performClick();
            assertFalse(((SwitchCompat) activity.findViewById(R.id.hide_status)).isChecked());
            assertFalse(((SwitchCompat) activity.findViewById(R.id.hide_navigation)).isChecked());
        }
    }
}
