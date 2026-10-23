package dev.androidbasekit.sample.model.repository;

import android.content.Context;
import android.content.SharedPreferences;

/** 欢迎介绍与功能引导的独立完成标记。独立文件只保存模板设置，不清理用户数据。 */
public final class LaunchPreferences {
    private static final String KEY_COMPLETED = "welcome_completed";
    private final SharedPreferences preferences;

    public LaunchPreferences(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences("java_launch_template", Context.MODE_PRIVATE);
    }

    public boolean isGuideCompleted() {
        return preferences.getBoolean("guide_completed", false);
    }

    public void setGuideCompleted(boolean completed) {
        preferences.edit().putBoolean("guide_completed", completed).apply();
    }

    public boolean isWelcomeCompleted() {
        return preferences.getBoolean(KEY_COMPLETED, false);
    }

    public void setWelcomeCompleted(boolean completed) {
        preferences.edit().putBoolean(KEY_COMPLETED, completed).apply();
    }
}
