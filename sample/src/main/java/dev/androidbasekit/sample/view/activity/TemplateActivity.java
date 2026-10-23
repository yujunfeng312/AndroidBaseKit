package dev.androidbasekit.sample.view.activity;

import android.os.Bundle;
import android.content.Context;
import android.content.res.Configuration;
import android.os.LocaleList;
import android.os.Build;
import java.util.Locale;
import androidx.core.graphics.Insets;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.viewbinding.ViewBinding;
import dev.androidbasekit.ui.BaseActivity;
import dev.androidbasekit.ui.BindingInflater;

/** 模板共用的系统栏适配；页面内容通过 onPageCreated 初始化。 */
public abstract class TemplateActivity<VB extends ViewBinding> extends BaseActivity<VB> {
    protected TemplateActivity(BindingInflater<VB> inflater) {
        super(inflater);
    }

    /** 仅为 Sample 页面指定中文，让原生日期、时间及媒体控件也使用中文资源。 */
    @Override protected void attachBaseContext(Context base) {
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        configuration.setLocales(new LocaleList(Locale.SIMPLIFIED_CHINESE));
        super.attachBaseContext(base.createConfigurationContext(configuration));
    }

    @Override protected final void onBindingCreated(Bundle state) {
        // 新系统的透明系统栏使用页面背景，默认图标明暗随 DayNight 背景同步。
        if (Build.VERSION.SDK_INT >= 35) {
            boolean light = ColorUtils.calculateLuminance(getColor(dev.androidbasekit.ui.R.color.color_background)) > .5;
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(light);
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(light);
        }
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(getBinding().getRoot());
        onPageCreated(state);
    }

    protected abstract void onPageCreated(Bundle state);
}
