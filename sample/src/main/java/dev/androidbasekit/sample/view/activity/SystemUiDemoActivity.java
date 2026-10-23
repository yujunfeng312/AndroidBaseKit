package dev.androidbasekit.sample.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import dev.androidbasekit.sample.R;
import dev.androidbasekit.sample.databinding.ActivitySystemUiDemoBinding;
import dev.androidbasekit.system.Keyboard;
import dev.androidbasekit.system.SystemIntents;

/** 仅操作当前演示窗口的系统栏、亮度与亮屏状态；不写入手机全局设置。 */
public final class SystemUiDemoActivity extends TemplateActivity<ActivitySystemUiDemoBinding> {
    private WindowInsetsControllerCompat controller;
    private boolean applying;
    private final int[] colorResources = {
            dev.androidbasekit.ui.R.color.color_transparent, R.color.system_ui_light,
            R.color.system_ui_blue, R.color.system_ui_dark
    };

    public SystemUiDemoActivity() { super(ActivitySystemUiDemoBinding::inflate); }

    @Override protected void onPageCreated(Bundle state) {
        prepareWindow();
        controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        getBinding().toolbar.setNavigationOnClickListener(view -> finish());
        // 替换模板默认的根布局 padding。底色层保持全屏，安全边距只作用于操作区域。
        getBinding().getRoot().setPadding(0, 0, 0, 0);
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (view, insets) -> {
            Insets safe = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            getBinding().content.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            getBinding().barBackdrop.setBarInsets(insets.getInsets(WindowInsetsCompat.Type.statusBars()),
                    insets.getInsets(WindowInsetsCompat.Type.navigationBars()));
            float density = getResources().getDisplayMetrics().density;
            getBinding().actualState.setText(getString(R.string.system_ui_actual_state,
                    getString(insets.isVisible(WindowInsetsCompat.Type.statusBars())
                            ? R.string.system_ui_visible : R.string.system_ui_hidden),
                    getString(insets.isVisible(WindowInsetsCompat.Type.navigationBars())
                            ? R.string.system_ui_visible : R.string.system_ui_hidden),
                    Math.round(safe.top / density), Math.round(safe.bottom / density)));
            return insets;
        });
        restoreControls(state);
        bindControls();
        applyWindowSettings();
        ViewCompat.requestApplyInsets(getBinding().getRoot());
    }

    @SuppressWarnings("deprecation")
    private void prepareWindow() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        // 新系统强制透明系统栏；旧系统显式设置透明，统一由页面在其下方绘制底色。
        if (Build.VERSION.SDK_INT < 35) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= 28) window.setNavigationBarDividerColor(Color.TRANSPARENT);
        }
        if (Build.VERSION.SDK_INT >= 29) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }
    }

    private void restoreControls(Bundle state) {
        getBinding().extendBackground.setChecked(state == null || state.getBoolean("extend", true));
        getBinding().hideStatus.setChecked(state != null && state.getBoolean("hide_status"));
        getBinding().hideNavigation.setChecked(state != null && state.getBoolean("hide_navigation"));
        getBinding().autoIcons.setChecked(state == null || state.getBoolean("auto_icons", true));
        getBinding().darkStatusIcons.setChecked(state != null && state.getBoolean("dark_status"));
        getBinding().darkNavigationIcons.setChecked(state != null && state.getBoolean("dark_navigation"));
        getBinding().keepAwake.setChecked(state != null && state.getBoolean("keep_awake"));
        getBinding().followBrightness.setChecked(state == null || state.getBoolean("follow_brightness", true));
        getBinding().brightness.setProgress(state == null ? 50 : state.getInt("brightness", 50));
        getBinding().statusColor.setSelection(state == null ? 0 : state.getInt("status_color"), false);
        getBinding().navigationColor.setSelection(state == null ? 0 : state.getInt("navigation_color"), false);
        getBinding().navigationSupport.setVisibility(Build.VERSION.SDK_INT < 26 ? View.VISIBLE : View.GONE);
    }

    private void bindControls() {
        getBinding().extendBackground.setOnCheckedChangeListener((view, checked) -> applyWindowSettings());
        getBinding().hideStatus.setOnCheckedChangeListener((view, checked) -> applyVisibility());
        getBinding().hideNavigation.setOnCheckedChangeListener((view, checked) -> applyVisibility());
        getBinding().autoIcons.setOnCheckedChangeListener((view, checked) -> applyWindowSettings());
        getBinding().darkStatusIcons.setOnCheckedChangeListener((view, checked) -> applyIcons());
        getBinding().darkNavigationIcons.setOnCheckedChangeListener((view, checked) -> applyIcons());
        getBinding().keepAwake.setOnCheckedChangeListener((view, checked) -> applyScreenSettings());
        getBinding().followBrightness.setOnCheckedChangeListener((view, checked) -> applyScreenSettings());
        AdapterView.OnItemSelectedListener colorListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyWindowSettings();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };
        getBinding().statusColor.setOnItemSelectedListener(colorListener);
        getBinding().navigationColor.setOnItemSelectedListener(colorListener);
        getBinding().brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar view, int value, boolean fromUser) { applyScreenSettings(); }
            @Override public void onStartTrackingTouch(SeekBar view) {}
            @Override public void onStopTrackingTouch(SeekBar view) {}
        });
        getBinding().immersive.setOnClickListener(view -> applyImmersivePreset(true));
        getBinding().showBars.setOnClickListener(view -> applyImmersivePreset(false));
        getBinding().reset.setOnClickListener(view -> {
            applying = true;
            restoreControls(null);
            applying = false;
            Keyboard.hide(getBinding().keyboardInput);
            applyWindowSettings();
        });
        getBinding().showKeyboard.setOnClickListener(view -> Keyboard.show(getBinding().keyboardInput));
        getBinding().hideKeyboard.setOnClickListener(view -> Keyboard.hide(getBinding().keyboardInput));
        getBinding().displaySettings.setOnClickListener(view -> openSettings(new Intent(Settings.ACTION_DISPLAY_SETTINGS)));
        getBinding().appSettings.setOnClickListener(view -> openSettings(SystemIntents.appSettings(this)));
        getBinding().notificationSettings.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= 26) openSettings(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
            else openSettings(SystemIntents.appSettings(this));
        });
    }

    private void applyImmersivePreset(boolean hide) {
        applying = true;
        if (hide) getBinding().extendBackground.setChecked(true);
        getBinding().hideStatus.setChecked(hide);
        getBinding().hideNavigation.setChecked(hide);
        applying = false;
        applyWindowSettings();
    }

    private int selectedColor(int position) {
        return getColor(colorResources[Math.max(0, Math.min(position, colorResources.length - 1))]);
    }

    private void applyWindowSettings() {
        if (applying) return;
        applying = true;
        boolean extended = getBinding().extendBackground.isChecked();
        int background = getColor(dev.androidbasekit.ui.R.color.color_background);
        if (extended) getBinding().getRoot().setBackgroundResource(R.drawable.system_ui_background);
        else getBinding().getRoot().setBackgroundColor(background);
        int titleColor = getColor(extended ? dev.androidbasekit.ui.R.color.color_white
                : dev.androidbasekit.ui.R.color.color_main_title);
        getBinding().toolbar.setTitleTextColor(titleColor);
        Drawable navigation = getBinding().toolbar.getNavigationIcon();
        if (navigation != null) navigation.mutate().setTint(titleColor);
        int status = selectedColor(getBinding().statusColor.getSelectedItemPosition());
        int nav = selectedColor(getBinding().navigationColor.getSelectedItemPosition());
        getBinding().barBackdrop.setBarColors(status, nav);
        boolean automatic = getBinding().autoIcons.isChecked();
        if (automatic) {
            int statusBase = extended ? getColor(R.color.system_ui_blue) : background;
            int navBase = extended ? getColor(R.color.system_ui_dark) : background;
            getBinding().darkStatusIcons.setChecked(ColorUtils.calculateLuminance(
                    ColorUtils.compositeColors(status, statusBase)) > .5);
            getBinding().darkNavigationIcons.setChecked(ColorUtils.calculateLuminance(
                    ColorUtils.compositeColors(nav, navBase)) > .5);
        }
        getBinding().darkStatusIcons.setEnabled(!automatic);
        getBinding().darkNavigationIcons.setEnabled(!automatic && Build.VERSION.SDK_INT >= 26);
        applying = false;
        applyIcons();
        applyVisibility();
        applyScreenSettings();
    }

    private void applyIcons() {
        if (applying) return;
        controller.setAppearanceLightStatusBars(getBinding().darkStatusIcons.isChecked());
        controller.setAppearanceLightNavigationBars(getBinding().darkNavigationIcons.isChecked());
    }

    private void applyVisibility() {
        if (applying) return;
        setBarVisible(WindowInsetsCompat.Type.statusBars(), !getBinding().hideStatus.isChecked());
        setBarVisible(WindowInsetsCompat.Type.navigationBars(), !getBinding().hideNavigation.isChecked());
    }

    private void setBarVisible(int type, boolean visible) {
        if (visible) controller.show(type);
        else controller.hide(type);
    }

    private void applyScreenSettings() {
        if (applying) return;
        if (getBinding().keepAwake.isChecked()) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        boolean follow = getBinding().followBrightness.isChecked();
        int brightness = Math.max(1, getBinding().brightness.getProgress());
        getBinding().brightness.setEnabled(!follow);
        getBinding().brightnessValue.setText(follow ? getString(R.string.system_ui_brightness_system)
                : getString(R.string.system_ui_brightness_value, brightness));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = follow ? WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE : brightness / 100f;
        getWindow().setAttributes(params);
    }

    private void openSettings(Intent intent) {
        if (!SystemIntents.startIfAvailable(this, intent)) Toast.makeText(this,
                R.string.system_ui_settings_unavailable, Toast.LENGTH_SHORT).show();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && controller != null) {
            applyIcons();
            applyVisibility();
        }
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean("extend", getBinding().extendBackground.isChecked());
        state.putBoolean("hide_status", getBinding().hideStatus.isChecked());
        state.putBoolean("hide_navigation", getBinding().hideNavigation.isChecked());
        state.putBoolean("auto_icons", getBinding().autoIcons.isChecked());
        state.putBoolean("dark_status", getBinding().darkStatusIcons.isChecked());
        state.putBoolean("dark_navigation", getBinding().darkNavigationIcons.isChecked());
        state.putBoolean("keep_awake", getBinding().keepAwake.isChecked());
        state.putBoolean("follow_brightness", getBinding().followBrightness.isChecked());
        state.putInt("brightness", getBinding().brightness.getProgress());
        state.putInt("status_color", getBinding().statusColor.getSelectedItemPosition());
        state.putInt("navigation_color", getBinding().navigationColor.getSelectedItemPosition());
        super.onSaveInstanceState(state);
    }
}
