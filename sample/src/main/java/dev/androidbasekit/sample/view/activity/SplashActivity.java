package dev.androidbasekit.sample.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import dev.androidbasekit.sample.databinding.ActivitySplashBinding;
import dev.androidbasekit.sample.model.repository.LaunchPreferences;

/** 项目启动页：短暂展示品牌内容，再按欢迎完成状态进入欢迎页或主页。 */
public final class SplashActivity extends TemplateActivity<ActivitySplashBinding> {
    private static final long DISPLAY_MILLIS = 1000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean routed;
    private final Runnable route = () -> {
        if (isFinishing() || routed) return;
        routed = true;
        Class<?> destination = new LaunchPreferences(this).isWelcomeCompleted()
                ? MainActivity.class : WelcomeActivity.class;
        startActivity(new Intent(this, destination));
        finish();
    };

    public SplashActivity() { super(ActivitySplashBinding::inflate); }

    @Override protected void onPageCreated(Bundle state) {
        if (!isTaskRoot() && Intent.ACTION_MAIN.equals(getIntent().getAction())
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)) {
            finish();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (!isFinishing() && !routed) handler.postDelayed(route, DISPLAY_MILLIS);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(route);
        super.onPause();
    }
}
